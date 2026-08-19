package com.lounge.domain.ownerchat.dto.response;

import com.lounge.domain.ownerproduct.entity.OwnedProduct;
import com.lounge.domain.product.entity.Product;

public record OwnedProductResponse(
        Long ownedProductId,
        Long productId,
        String sku,
        String name,
        String category,
        String imageUrl
) {

    public static OwnedProductResponse from(OwnedProduct ownedProduct) {
        Product product = ownedProduct.getProduct();

        return new OwnedProductResponse(
                ownedProduct.getId(),
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getImageUrl()
        );
    }
}
