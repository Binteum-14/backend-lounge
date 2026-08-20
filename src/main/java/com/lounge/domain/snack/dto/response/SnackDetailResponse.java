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
    /**
     * 선택한 메뉴에 실제로 연결된 수납 가방 ID입니다.
     * 간식은 L01~L07, 음료는 F01~F07, 향수는 P01~P07을 반환합니다.
     */
    private String packingProfileId;
    /** 선택한 메뉴의 라운지 화면 수납 가방 ID입니다. */
    private String loungePackingProfileId;
    /** 선택한 메뉴의 비행기 화면 수납 가방 ID입니다. */
    private String flightPackingProfileId;
    private String snackImageUrl;
    private String productImageUrl;

    public static SnackDetailResponse of(
            Snack snack,
            ProductVariant productVariant,
            String packingProfileId,
            String loungePackingProfileId,
            String flightPackingProfileId
    ) {
        return new SnackDetailResponse(
                productVariant.getId(),
                productVariant.getSku(),
                packingProfileId,
                loungePackingProfileId,
                flightPackingProfileId,
                snack.getImageUrl(),
                productVariant.getImageUrl()
        );
    }
}
