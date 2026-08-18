package com.lounge.domain.recommendation.controller;

import com.lounge.domain.recommendation.dto.request.RecommendationGenerateRequest;
import com.lounge.domain.recommendation.dto.response.RecommendationResponse;
import com.lounge.domain.recommendation.exception.code.RecommendationSuccessCode;
import com.lounge.domain.recommendation.service.RecommendationService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Recommendation",
        description = "MCM AI 제품 추천 API"
)
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "AI 제품 추천 생성",
            description = """
                    진단 결과와 이미 선정된 상위 제품 3개를 기반으로
                    OpenAI가 전체 분석과 제품별 추천 이유를 생성합니다.
                    생성된 결과는 Recommendation과
                    RecommendationProduct에 저장됩니다.
                    """
    )
    @PostMapping
    public ApiResponse<RecommendationResponse> generateRecommendation(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody
            RecommendationGenerateRequest request
    ) {
        return ApiResponse.onSuccess(
                RecommendationSuccessCode
                        .RECOMMENDATION_GENERATE_SUCCESS,
                recommendationService.generateRecommendation(
                        userId,
                        request
                )
        );
    }
}