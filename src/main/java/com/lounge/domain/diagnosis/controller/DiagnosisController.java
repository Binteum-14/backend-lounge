package com.lounge.domain.diagnosis.controller;

import com.lounge.domain.diagnosis.dto.request.DiagnosisCompleteRequest;
import com.lounge.domain.diagnosis.exception.code.DiagnosisSuccessCode;
import com.lounge.domain.diagnosis.service.DiagnosisService;
import com.lounge.domain.recommendation.dto.response.RecommendationResponse;
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

@Tag(name = "Diagnosis", description = "진단 API")
@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @Operation(
            summary = "진단 완료 및 제품 추천",
            description = """
                    문항 번호(questionNo)와 선택지 번호(answerNo)만 받습니다.
                    백엔드가 코드·문구를 매핑해 저장한 뒤 상위 3개 제품을 선정하고,
                    기존 추천 API와 동일하게 AI 추천 이유를 생성합니다.
                    """
    )
    @PostMapping
    public ApiResponse<RecommendationResponse> completeDiagnosis(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DiagnosisCompleteRequest request
    ) {
        return ApiResponse.onSuccess(
                DiagnosisSuccessCode.DIAGNOSIS_COMPLETE_SUCCESS,
                diagnosisService.complete(userId, request)
        );
    }
}
