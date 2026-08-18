package com.lounge.domain.recommendation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RecommendationGenerateRequest(

        @NotNull(message = "진단 ID는 필수입니다.")
        Long diagnosisId,

        @Valid
        @NotNull(message = "선정된 제품 정보는 필수입니다.")
        @Size(
                min = 3,
                max = 3,
                message = "선정된 제품은 정확히 3개여야 합니다."
        )
        List<SelectedProductRequest> selectedProducts

) {

    public record SelectedProductRequest(

            @NotNull(message = "제품 ID는 필수입니다.")
            Long productId,

            @NotNull(message = "추천 순위는 필수입니다.")
            @Min(value = 1, message = "추천 순위는 1 이상이어야 합니다.")
            @Max(value = 3, message = "추천 순위는 3 이하여야 합니다.")
            Integer recommendationRank,

            @NotNull(message = "매칭 점수는 필수입니다.")
            @Min(value = 0, message = "매칭 점수는 0 이상이어야 합니다.")
            @Max(value = 100, message = "매칭 점수는 100 이하여야 합니다.")
            Integer matchScore
    ) {
    }
}