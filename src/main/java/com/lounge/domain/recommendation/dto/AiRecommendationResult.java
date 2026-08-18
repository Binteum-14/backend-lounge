package com.lounge.domain.recommendation.dto;

import java.util.List;

public record AiRecommendationResult(
        String resultSummary,
        List<ProductReason> products
) {

    public record ProductReason(
            Long productId,
            String recommendationReason
    ) {
    }
}