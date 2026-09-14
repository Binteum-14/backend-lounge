package com.lounge.domain.product.dto.response;

import com.lounge.domain.packing.dto.PackingProfile;
import com.lounge.domain.product.entity.Product;
import com.lounge.domain.product.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {

    private String variantImageUrl;
    private String name;
    private Long price;
    private String description;
    private String detailUrl;
    private Long productVariantId;
    private String productSku;
    /** 수납 지원 상품일 때만 값이 있습니다. */
    private String packingProfileId;
    /** 선택한 상품 SKU로 찾은 실제 수납 프로필입니다. */
    private PackingProfile packingProfile;

    public static ProductResponse from(ProductVariant productVariant, PackingProfile packingProfile) {
        Product product = productVariant.getProduct();
        return new ProductResponse(
                productVariant.getImageUrl(),
                product.getName(),
                productVariant.getPrice(),
                product.getDescription(),
                productVariant.getDetailUrl(),
                productVariant.getId(),
                productVariant.getSku(),
                packingProfile == null ? null : packingProfile.getPackingProfileId(),
                packingProfile
        );
    }
}
