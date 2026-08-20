package com.lounge.domain.packing.service;

import com.lounge.domain.packing.PackingStatus;
import com.lounge.domain.packing.dto.PackingCheckRequest;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import com.lounge.domain.packing.dto.PackingItemDefinition;
import com.lounge.domain.packing.dto.PackingProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PackingService {

    /**
     * 실제 가방 전체 외형 부피 중
     * 실제 수납 공간으로 활용할 수 있다고 보는 비율.
     *
     * 안감, 모서리, 지퍼 구조 등을 고려한 보수적인 값이다.
     */
    private static final double USABLE_VOLUME_RATIO = 0.72;

    /**
     * 제품 외형 치수와 실제 내부 치수의 차이를 고려한다.
     */
    private static final double DIMENSION_SAFETY_RATIO = 0.97;

    private final CarryItemCatalog carryItemCatalog;

    private final PackingProfileCatalog packingProfileCatalog;

    private final PackingLayoutEngine packingLayoutEngine;

    public List<PackingItemDefinition> getAvailableItems() {
        return carryItemCatalog.findAll();
    }

    public List<PackingProfile> getAvailableProfiles() {
        return packingProfileCatalog.findAll();
    }

    public PackingCheckResponse check(
            String loungeId,
            PackingCheckRequest request
    ) {
        PackingProfile profile = packingProfileCatalog.findByLoungeId(loungeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "수납 프로필을 찾을 수 없습니다: " + loungeId
                ));

        BagDimensions bag = BagDimensions.from(profile);

        List<PackingItemDefinition> selectedItems =
                resolveItems(request.itemCodes());

        List<EvaluatedItem> dimensionEvaluatedItems =
                evaluateItems(
                        selectedItems,
                        profile,
                        bag
                );

        double usableBagVolume =
                bag.volume()
                        * USABLE_VOLUME_RATIO;

        List<EvaluatedItem> evaluatedItems =
                applyCombinedCapacity(
                        dimensionEvaluatedItems,
                        usableBagVolume
                );

        double totalItemVolume =
                evaluatedItems.stream()
                        .filter(EvaluatedItem::fit)
                        .map(EvaluatedItem::item)
                        .mapToDouble(PackingItemDefinition::volume)
                        .sum();

        double usedSpaceRatio =
                usableBagVolume <= 0
                        ? 0
                        : totalItemVolume
                        / usableBagVolume;

        int notFitCount =
                (int) evaluatedItems.stream()
                        .filter(item -> !item.fit())
                        .count();

        int tightCount =
                (int) evaluatedItems.stream()
                        .filter(
                                item ->
                                        item.fit()
                                                && item.tight()
                        )
                        .count();

        PackingStatus status =
                determineStatus(
                        usedSpaceRatio,
                        notFitCount,
                        tightCount
                );

        int fitScore =
                calculateFitScore(
                        usedSpaceRatio,
                        notFitCount,
                        tightCount
                );

        List<PackingCheckResponse.ItemResult> itemResults =
                evaluatedItems.stream()
                        .map(this::toItemResult)
                        .toList();

        List<PackingCheckResponse.Placement> placements =
                buildPlacements(
                        evaluatedItems,
                        bag
                );

        return new PackingCheckResponse(
                profile.loungeId(),
                profile.scene(),
                profile.sku(),
                profile.productName(),
                profile.color(),
                profile.imagePath(),
                profile.getImageUrl(),
                status,
                fitScore,
                round(usedSpaceRatio),
                new PackingCheckResponse.BagSize(
                        round(bag.widthMm()),
                        round(bag.heightMm()),
                        round(bag.depthMm())
                ),
                itemResults,
                placements,
                buildNotice(status),
                profile.sourceUrl()
        );
    }

    /**
     * Builds a useful, visually full packing preset without adding an item
     * that the bag cannot accommodate. The target is deliberately below full
     * capacity so the recommendation remains practical for real use.
     */
    public PackingCheckResponse recommend(String loungeId) {
        PackingProfile profile = packingProfileCatalog.findByLoungeId(loungeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "수납 프로필을 찾을 수 없습니다: " + loungeId
                ));

        List<String> suggestedItemCodes = new ArrayList<>();
        for (String candidate : recommendationCandidates(profile)) {
            List<String> nextItemCodes = new ArrayList<>(suggestedItemCodes);
            nextItemCodes.add(candidate);

            PackingCheckResponse nextResult = check(
                    loungeId,
                    new PackingCheckRequest(nextItemCodes)
            );

            boolean allFit = nextResult.items().stream()
                    .allMatch(PackingCheckResponse.ItemResult::fit);
            if (!allFit) {
                continue;
            }

            suggestedItemCodes = nextItemCodes;
            if (nextResult.usedSpaceRatio() >= 0.55) {
                return nextResult;
            }
        }

        return check(loungeId, new PackingCheckRequest(suggestedItemCodes));
    }

    private List<String> recommendationCandidates(PackingProfile profile) {
        if (profile.volumeCm3() <= 5_000) {
            return List.of(
                    "SMARTPHONE",
                    "CARD_WALLET",
                    "EARBUDS_CASE",
                    "KEY_CASE",
                    "USB_C_CHARGER",
                    "CHARGING_CABLE",
                    "SUNGLASSES_CASE",
                    "POWER_BANK",
                    "PASSPORT",
                    "POUCH",
                    "NOTEBOOK_A5",
                    "BOOK_PAPERBACK"
            );
        }

        return List.of(
                "SMARTPHONE",
                "CARD_WALLET",
                "POUCH",
                "BOOK_PAPERBACK",
                "TUMBLER",
                "CAMERA",
                "NOTEBOOK_A5",
                "HEADPHONES_CASE",
                "POWER_BANK",
                "SUNGLASSES_CASE",
                "PASSPORT",
                "KEY_CASE",
                "USB_C_CHARGER",
                "CHARGING_CABLE",
                "EARBUDS_CASE",
                "TABLET_11",
                "LAPTOP_13",
                "LAPTOP_15"
        );
    }

    private List<PackingItemDefinition> resolveItems(
            List<String> itemCodes
    ) {

        Set<String> uniqueCodes =
                new LinkedHashSet<>();

        for (String itemCode : itemCodes) {

            if (itemCode != null) {
                uniqueCodes.add(
                        itemCode.trim().toUpperCase()
                );
            }
        }

        List<PackingItemDefinition> result =
                new ArrayList<>();

        for (String code : uniqueCodes) {

            PackingItemDefinition item =
                    carryItemCatalog.findByCode(code)
                            .orElseThrow(
                                    () -> new ResponseStatusException(
                                            HttpStatus.BAD_REQUEST,
                                            "지원하지 않는 물건 코드입니다: "
                                                    + code
                                    )
                            );

            result.add(item);
        }

        return result;
    }

    private List<EvaluatedItem> evaluateItems(
            List<PackingItemDefinition> items,
            PackingProfile profile,
            BagDimensions bag
    ) {

        List<EvaluatedItem> results =
                new ArrayList<>();

        double usableWidth =
                bag.widthMm()
                        * DIMENSION_SAFETY_RATIO;

        double usableHeight =
                bag.heightMm()
                        * DIMENSION_SAFETY_RATIO;

        double usableDepth =
                bag.depthMm()
                        * DIMENSION_SAFETY_RATIO;

        for (PackingItemDefinition item : items) {

            if (isEverydayEssential(item)) {
                results.add(new EvaluatedItem(
                        item,
                        true,
                        false,
                        new Orientation(
                                item.widthMm(),
                                item.heightMm(),
                                item.depthMm()
                        ),
                        "일상 소지품 수납 기준으로 사용할 수 있습니다."
                ));
                continue;
            }

            String compatibilityReason =
                    deviceCompatibilityReason(
                            item,
                            profile
                    );

            if (compatibilityReason != null) {

                results.add(
                        new EvaluatedItem(
                                item,
                                false,
                                false,
                                null,
                                compatibilityReason
                        )
                );

                continue;
            }

            Orientation orientation =
                    findBestOrientation(
                            item,
                            usableWidth,
                            usableHeight,
                            usableDepth
                    );

            if (orientation == null) {

                results.add(
                        new EvaluatedItem(
                                item,
                                false,
                                false,
                                null,
                                "제품 크기 기준으로 안정적인 수납 방향을 찾지 못했습니다."
                        )
                );

                continue;
            }

            double widthRatio =
                    orientation.widthMm()
                            / usableWidth;

            double heightRatio =
                    orientation.heightMm()
                            / usableHeight;

            double depthRatio =
                    orientation.depthMm()
                            / usableDepth;

            double maxRatio =
                    Math.max(
                            widthRatio,
                            Math.max(
                                    heightRatio,
                                    depthRatio
                            )
                    );

            boolean tight =
                    maxRatio >= 0.90;

            String reason =
                    tight
                            ? "수납은 가능하지만 한 방향의 여유 공간이 적습니다."
                            : "제품 크기 기준으로 수납 가능한 범위입니다.";

            results.add(
                    new EvaluatedItem(
                            item,
                            true,
                            tight,
                            orientation,
                            reason
                    )
            );
        }

        return results;
    }

    /**
     * Phones, wallets, and slim pouches are everyday essentials. Their
     * flexible placement should not make a bag look unusable; the combined
     * capacity calculation below still prevents an unrealistic overfill.
     */
    private boolean isEverydayEssential(PackingItemDefinition item) {
        return switch (item.code()) {
            case "SMARTPHONE", "CARD_WALLET", "POUCH" -> true;
            default -> false;
        };
    }

    /**
     * A bag can be large enough for each selected item on its own but still
     * lack the combined usable volume. Preserve the request order so the item
     * the user just added becomes the one reported as unavailable.
     */
    private List<EvaluatedItem> applyCombinedCapacity(
            List<EvaluatedItem> evaluatedItems,
            double usableBagVolume
    ) {
        List<EvaluatedItem> result = new ArrayList<>();
        double occupiedVolume = 0;

        for (EvaluatedItem evaluated : evaluatedItems) {
            if (!evaluated.fit()) {
                result.add(evaluated);
                continue;
            }

            double nextVolume = occupiedVolume + evaluated.item().volume();
            if (nextVolume > usableBagVolume) {
                result.add(new EvaluatedItem(
                        evaluated.item(),
                        false,
                        false,
                        null,
                        "이미 선택한 물건과 함께 넣으면 예상 수납 공간을 초과합니다."
                ));
                continue;
            }

            boolean tight = evaluated.tight()
                    || nextVolume / usableBagVolume >= 0.75;
            String reason = tight
                    ? "수납은 가능하지만 남은 공간이 적습니다."
                    : evaluated.reason();

            result.add(new EvaluatedItem(
                    evaluated.item(),
                    true,
                    tight,
                    evaluated.orientation(),
                    reason
            ));
            occupiedVolume = nextVolume;
        }

        return result;
    }

    private String deviceCompatibilityReason(
            PackingItemDefinition item,
            PackingProfile profile
    ) {

        return switch (item.code()) {

            case "LAPTOP_13" ->
                    !Boolean.TRUE.equals(
                            profile.laptopSupported()
                    )
                            ? "이 가방은 노트북 수납을 지원하는 프로필이 아닙니다."
                            : profile.laptopMaxInches() != null
                            && profile.laptopMaxInches() < 13
                            ? "공식 노트북 수납 한도를 초과합니다."
                            : null;

            case "LAPTOP_15" ->
                    !Boolean.TRUE.equals(
                            profile.laptopSupported()
                    )
                            ? "이 가방은 노트북 수납을 지원하는 프로필이 아닙니다."
                            : profile.laptopMaxInches() == null
                            || profile.laptopMaxInches() < 15
                            ? "공식 노트북 수납 한도를 초과합니다."
                            : null;

            case "TABLET_11" ->
                    !Boolean.TRUE.equals(
                            profile.tabletSupported()
                    )
                            ? "이 가방은 태블릿 수납을 지원하는 프로필이 아닙니다."
                            : null;

            default -> null;
        };
    }

    /**
     * 물건의 방향을 바꿔가며
     * 가방 안에 들어갈 수 있는지 검사한다.
     */
    private Orientation findBestOrientation(
            PackingItemDefinition item,
            double bagWidth,
            double bagHeight,
            double bagDepth
    ) {

        double w = item.widthMm();
        double h = item.heightMm();
        double d = item.depthMm();

        Orientation[] orientations = {

                new Orientation(w, h, d),
                new Orientation(w, d, h),

                new Orientation(h, w, d),
                new Orientation(h, d, w),

                new Orientation(d, w, h),
                new Orientation(d, h, w)
        };

        Orientation best = null;
        double bestScore =
                Double.MAX_VALUE;

        for (Orientation orientation : orientations) {

            if (orientation.widthMm() > bagWidth
                    || orientation.heightMm() > bagHeight
                    || orientation.depthMm() > bagDepth) {

                continue;
            }

            double widthRatio =
                    orientation.widthMm()
                            / bagWidth;

            double heightRatio =
                    orientation.heightMm()
                            / bagHeight;

            double depthRatio =
                    orientation.depthMm()
                            / bagDepth;

            double score =
                    Math.max(
                            widthRatio,
                            Math.max(
                                    heightRatio,
                                    depthRatio
                            )
                    );

            if (score < bestScore) {
                bestScore = score;
                best = orientation;
            }
        }

        return best;
    }

    private PackingStatus determineStatus(
            double usedSpaceRatio,
            int notFitCount,
            int tightCount
    ) {

        if (notFitCount > 0
                || usedSpaceRatio > 0.95) {

            return PackingStatus.NOT_RECOMMENDED;
        }

        if (tightCount > 0
                || usedSpaceRatio > 0.75) {

            return PackingStatus.TIGHT;
        }

        return PackingStatus.COMFORTABLE;
    }

    private int calculateFitScore(
            double usedSpaceRatio,
            int notFitCount,
            int tightCount
    ) {

        double score = 100;

        score -= Math.min(
                usedSpaceRatio,
                1.5
        ) * 35;

        score -= tightCount * 6;

        score -= notFitCount * 30;

        if (usedSpaceRatio > 1.0) {

            score -=
                    (usedSpaceRatio - 1.0)
                            * 25;
        }

        return (int) Math.round(
                Math.max(
                        0,
                        Math.min(
                                100,
                                score
                        )
                )
        );
    }

    private PackingCheckResponse.ItemResult toItemResult(
            EvaluatedItem evaluated
    ) {

        PackingItemDefinition item =
                evaluated.item();

        return new PackingCheckResponse.ItemResult(
                item.code(),
                item.name(),
                evaluated.fit(),
                evaluated.tight(),
                item.widthMm(),
                item.heightMm(),
                item.depthMm(),
                evaluated.reason()
        );
    }

    /**
     * 실제 물리 시뮬레이션 좌표가 아니라
     * 프론트 시각화를 위한 상대 배치 좌표를 생성한다.
     */
    private List<PackingCheckResponse.Placement> buildPlacements(
            List<EvaluatedItem> evaluatedItems,
            BagDimensions bag
    ) {

        List<PackingLayoutEngine.LayoutItem> layoutItems =
                evaluatedItems.stream()
                        .filter(EvaluatedItem::fit)
                        .map(item ->
                                new PackingLayoutEngine.LayoutItem(
                                        item.item().code(),
                                        item.item().name(),
                                        // The fit calculation may rotate an item in depth,
                                        // but the X-ray is a front-facing visual. Keep the
                                        // product's native width and height here so a laptop,
                                        // book, and phone retain their real screen proportion.
                                        item.item().widthMm(),
                                        item.item().heightMm()
                                )
                        )
                        .toList();

        return packingLayoutEngine.layout(
                layoutItems,
                bag.widthMm(),
                bag.heightMm()
        );
    }

    private String buildNotice(
            PackingStatus status
    ) {

        return switch (status) {

            case COMFORTABLE ->
                    "제품 데이터와 대표 소지품 크기를 기준으로 여유 있는 수납이 예상됩니다. 실제 수납감은 제품 구조와 소지품 형태에 따라 달라질 수 있습니다.";

            case TIGHT ->
                    "수납은 가능하지만 일부 공간이 빠듯할 수 있습니다. 실제 제품의 입구 형태와 내부 구조에 따라 차이가 발생할 수 있습니다.";

            case NOT_RECOMMENDED ->
                    "선택한 물건 중 일부가 제품 크기에 비해 크거나 전체 수납 공간이 부족할 가능성이 있습니다.";

            case PROFILE_UNAVAILABLE ->
                    "제품 크기 정보가 부족하여 수납 분석을 진행할 수 없습니다.";
        };
    }

    private double round(double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private record Orientation(
            double widthMm,
            double heightMm,
            double depthMm
    ) {
    }

    private record BagDimensions(
            double widthMm,
            double heightMm,
            double depthMm
    ) {

        private static BagDimensions from(PackingProfile profile) {
            return new BagDimensions(
                    profile.widthCm() * 10,
                    profile.heightCm() * 10,
                    profile.depthCm() * 10
            );
        }

        private double volume() {
            return widthMm * heightMm * depthMm;
        }
    }

    private record EvaluatedItem(
            PackingItemDefinition item,
            boolean fit,
            boolean tight,
            Orientation orientation,
            String reason
    ) {
    }
}
