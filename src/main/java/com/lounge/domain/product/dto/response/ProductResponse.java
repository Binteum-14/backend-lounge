package com.lounge.domain.product.dto.response;

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

    public static ProductResponse from(ProductVariant productVariant) {
        Product product = productVariant.getProduct();
        return new ProductResponse(
                productVariant.getImageUrl(),
                product.getName(),
                productVariant.getPrice(),
                product.getDescription(),
                productVariant.getDetailUrl()
        );
    }
}
