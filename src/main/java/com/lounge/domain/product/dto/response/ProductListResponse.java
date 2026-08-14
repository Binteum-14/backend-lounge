package com.lounge.domain.product.dto.response;

import com.lounge.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductListResponse {

    private Long productId;
    private String name;
    private String imageUrl;

    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getImageUrl()
        );
    }
}
