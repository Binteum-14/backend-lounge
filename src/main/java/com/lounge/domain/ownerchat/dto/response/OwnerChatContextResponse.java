package com.lounge.domain.ownerchat.dto.response;

import java.util.List;

public record OwnerChatContextResponse(
        Long ownedProductId,
        Long productId,
        String sku,
        String name,
        String category,
        String imageUrl,
        String greeting,
        List<String> suggestedQuestions
) {
}
