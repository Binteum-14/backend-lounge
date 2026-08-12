package com.lounge.domain.snack.dto.response;

import com.lounge.domain.product.entity.Product;
import com.lounge.domain.snack.entity.Snack;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnackDetailResponse {

    private String snackImageUrl;
    private String productImageUrl;

    public static SnackDetailResponse of(Snack snack, Product product) {
        return new SnackDetailResponse(
                snack.getImageUrl(),
                product.getImageUrl()
        );
    }
}
