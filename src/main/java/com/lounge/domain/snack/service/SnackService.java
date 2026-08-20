package com.lounge.domain.snack.service;

import com.lounge.domain.product.entity.ProductVariant;
import com.lounge.domain.snack.dto.response.SnackDetailResponse;
import com.lounge.domain.snack.dto.response.SnackResponse;
import com.lounge.domain.snack.entity.Snack;
import com.lounge.domain.snack.entity.SnackSet;
import com.lounge.domain.snack.exception.SnackException;
import com.lounge.domain.snack.exception.code.SnackErrorCode;
import com.lounge.domain.snack.repository.SnackRepository;
import com.lounge.domain.snack.repository.SnackSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnackService {

    private final SnackRepository snackRepository;
    private final SnackSetRepository snackSetRepository;

    public List<SnackResponse> getSnacks() {
        return snackRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(SnackResponse::from)
                .toList();
    }

    public SnackDetailResponse getSnack(Long snackId) {
        Snack snack = snackRepository.findByIdAndActiveTrue(snackId)
                .orElseThrow(() -> SnackException.of(SnackErrorCode.SNACK_NOT_FOUND));

        SnackSet snackSet = snackSetRepository.findBySnack_Id(snackId)
                .orElseThrow(() -> SnackException.of(SnackErrorCode.SNACK_SET_NOT_FOUND));

        ProductVariant productVariant = snackSet.getProductVariant();
        if (productVariant == null) {
            throw SnackException.of(SnackErrorCode.SNACK_PRODUCT_NOT_FOUND);
        }

        int menuOrder = getMenuOrder(snack);
        String loungePackingProfileId = "L%02d".formatted(menuOrder);
        String flightPackingProfileId = "F%02d".formatted(menuOrder);
        String packingProfileId = switch (snack.getType()) {
            case SNACK -> loungePackingProfileId;
            case DRINK -> flightPackingProfileId;
            case PERFUME -> "P%02d".formatted(menuOrder);
        };

        return SnackDetailResponse.of(
                snack,
                productVariant,
                packingProfileId,
                loungePackingProfileId,
                flightPackingProfileId
        );
    }

    /**
     * 음료, 간식, 향수는 각각 독립된 메뉴 순서를 갖지만,
     * 메뉴별로 같은 순번의 전용 수납 가방을 연결합니다.
     * 간식은 L01~L07, 음료는 F01~F07, 향수는 P01~P07을 사용합니다.
     */
    private int getMenuOrder(Snack snack) {
        List<Snack> snacksOfSameType = snackRepository
                .findByTypeAndActiveTrueOrderByIdAsc(snack.getType());

        int index = snacksOfSameType.stream()
                .map(Snack::getId)
                .toList()
                .indexOf(snack.getId());

        if (index < 0 || index >= 7) {
            throw new IllegalStateException(
                    "수납 프로필에 연결할 수 없는 메뉴입니다: " + snack.getId()
            );
        }

        return index + 1;
    }
}
