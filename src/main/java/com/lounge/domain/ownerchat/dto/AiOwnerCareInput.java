package com.lounge.domain.ownerchat.dto;

import java.util.List;

public record AiOwnerCareInput(
        ProductContext product,
        List<Message> history,
        String question
) {

    public record ProductContext(
            Long productId,
            String sku,
            String name,
            String category,
            String description,
            String productFeature,
            String careGuide
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
