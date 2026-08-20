package com.lounge.domain.recommendation.dto.response;

import java.util.List;

public record RecommendationResponse(

        Long recommendationId,
        String resultSummary,
        List<RecommendedProductResponse> products

) {

    public record RecommendedProductResponse(

            Long recommendationProductId,
            Long productId,
            String name,
            String imageUrl,
            String detailUrl,
            Integer recommendationRank,
            Integer matchScore,
            String recommendationReason

    ) {
    }
}