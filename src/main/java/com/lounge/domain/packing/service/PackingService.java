package com.lounge.domain.packing.service;

import com.lounge.domain.packing.PackingStatus;
import com.lounge.domain.packing.dto.PackingCheckRequest;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import com.lounge.domain.packing.dto.PackingItemDefinition;
import com.lounge.domain.product.entity.Product;
import com.lounge.domain.product.repository.ProductRepository;
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

    private final ProductRepository productRepository;

    private final CarryItemCatalog carryItemCatalog;

    private final ProductDimensionExtractor dimensionExtractor;

    public List<PackingItemDefinition> getAvailableItems() {
        return carryItemCatalog.findAll();
    }

    public PackingCheckResponse check(
            Long productId,
            PackingCheckRequest request
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "제품을 찾을 수 없습니다."
                        )
                );

        ProductDimensionExtractor.BagDimensions bag =
                dimensionExtractor.extract(product)
                        .orElse(null);

        if (bag == null) {

            return new PackingCheckResponse(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    PackingStatus.PROFILE_UNAVAILABLE,
                    0,
                    0.0,
                    null,
                    List.of(),
                    List.of(),
                    "제품 데이터에서 크기 정보를 찾지 못했습니다."
            );
        }

        List<PackingItemDefinition> selectedItems =
                resolveItems(request.itemCodes());

        List<EvaluatedItem> evaluatedItems =
                evaluateItems(
                        selectedItems,
                        bag
                );

        double totalItemVolume =
                selectedItems.stream()
                        .mapToDouble(
                                PackingItemDefinition::volume
                        )
                        .sum();

        double usableBagVolume =
                bag.volume()
                        * USABLE_VOLUME_RATIO;

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
                product.getId(),
                product.getName(),
                product.getImageUrl(),
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
                buildNotice(status)
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
            ProductDimensionExtractor.BagDimensions bag
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
            ProductDimensionExtractor.BagDimensions bag
    ) {

        List<PackingCheckResponse.Placement> result =
                new ArrayList<>();

        double x = 5;
        double y = 5;

        double currentRowHeight = 0;

        for (EvaluatedItem evaluated : evaluatedItems) {

            if (!evaluated.fit()
                    || evaluated.orientation() == null) {

                continue;
            }

            Orientation orientation =
                    evaluated.orientation();

            double widthPercent =
                    orientation.widthMm()
                            / bag.widthMm()
                            * 82;

            double heightPercent =
                    orientation.heightMm()
                            / bag.heightMm()
                            * 82;

            widthPercent =
                    clamp(
                            widthPercent,
                            12,
                            82
                    );

            heightPercent =
                    clamp(
                            heightPercent,
                            10,
                            72
                    );

            if (x + widthPercent > 95) {

                x = 5;
                y += currentRowHeight + 4;
                currentRowHeight = 0;
            }

            if (y + heightPercent > 95) {

                y = Math.max(
                        5,
                        95 - heightPercent
                );
            }

            result.add(
                    new PackingCheckResponse.Placement(
                            evaluated.item().code(),
                            evaluated.item().name(),
                            round(x),
                            round(y),
                            round(widthPercent),
                            round(heightPercent),
                            0
                    )
            );

            x += widthPercent + 4;

            currentRowHeight =
                    Math.max(
                            currentRowHeight,
                            heightPercent
                    );
        }

        return result;
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

    private double clamp(
            double value,
            double min,
            double max
    ) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
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

    private record EvaluatedItem(
            PackingItemDefinition item,
            boolean fit,
            boolean tight,
            Orientation orientation,
            String reason
    ) {
    }
}