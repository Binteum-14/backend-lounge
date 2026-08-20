package com.lounge.domain.snack.dto.response;

import com.lounge.domain.product.entity.ProductVariant;
import com.lounge.domain.snack.entity.Snack;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnackDetailResponse {

    private Long productVariantId;
    private String productSku;
    private String loungePackingProfileId;
    private String flightPackingProfileId;
    private String snackImageUrl;
    private String productImageUrl;

    public static SnackDetailResponse of(
            Snack snack,
            ProductVariant productVariant,
            String loungePackingProfileId,
            String flightPackingProfileId
    ) {
        return new SnackDetailResponse(
                productVariant.getId(),
                productVariant.getSku(),
                loungePackingProfileId,
                flightPackingProfileId,
                snack.getImageUrl(),
                productVariant.getImageUrl()
        );
    }
}
