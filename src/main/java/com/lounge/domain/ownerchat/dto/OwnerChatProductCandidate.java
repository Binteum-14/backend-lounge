package com.lounge.domain.ownerchat.dto;

import com.lounge.domain.product.entity.Product;

public record OwnerChatProductCandidate(

        Long productId,

        String sku,

        String name

) {

    public static OwnerChatProductCandidate from(Product product) {

        return new OwnerChatProductCandidate(
                product.getId(),
                product.getSku(),
                product.getName()
        );
    }
}