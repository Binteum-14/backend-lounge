package com.lounge.domain.recommendation.dto;

import java.util.List;

public record AiRecommendationInput(
        List<Answer> answers,
        List<CandidateProduct> products
) {

    public record Answer(
            String questionCode,
            String answerText
    ) {
    }

    public record CandidateProduct(
            Long productId,
            String name,
            String category,
            Long price,
            String description,
            Integer storageScore,
            Integer versatilityScore,
            Integer travelSuitabilityScore,
            Integer commuteSuitabilityScore,
            Boolean laptopStorageAvailable,
            Integer laptopStorageScore,
            Integer cabinSuitabilityScore
    ) {
    }
}