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

        return SnackDetailResponse.of(snack, productVariant);
    }
}
