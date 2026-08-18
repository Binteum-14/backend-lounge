package com.lounge.domain.snack.dto.response;

import com.lounge.domain.product.entity.ProductVariant;
import com.lounge.domain.snack.entity.Snack;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnackDetailResponse {

    private String snackImageUrl;
    private String productImageUrl;

    public static SnackDetailResponse of(Snack snack, ProductVariant productVariant) {
        return new SnackDetailResponse(
                snack.getImageUrl(),
                productVariant.getImageUrl()
        );
    }
}
