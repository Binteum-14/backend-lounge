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

        return SnackDetailResponse.of(
                snack,
                productVariant,
                loungePackingProfileId,
                flightPackingProfileId
        );
    }

    /**
     * The focus-mode dataset pairs drink, dessert, and fragrance menus by
     * their order: each category's 1st item maps to L01/F01, and so on.
     */
    private int getMenuOrder(Snack snack) {
        List<Snack> snacksOfSameType = snackRepository
                .findByTypeOrderByIdAsc(snack.getType());

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
