package com.lounge.domain.visitpass.controller;

import com.lounge.domain.visitpass.dto.request.VisitPassIssueRequest;
import com.lounge.domain.visitpass.dto.response.VisitPassListResponse;
import com.lounge.domain.visitpass.dto.response.VisitPassResponse;
import com.lounge.domain.visitpass.exception.code.VisitPassSuccessCode;
import com.lounge.domain.visitpass.service.VisitPassService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "VisitPass", description = "방문 패스 API")
@RestController
@RequestMapping("/api/visit-passes")
@RequiredArgsConstructor
public class VisitPassController {

    private final VisitPassService visitPassService;

    @Operation(summary = "방문 패스 발급", description = "선택한 추천 상품으로 Visit Pass를 발급하고 QR 이미지를 생성합니다.")
    @PostMapping
    public ApiResponse<VisitPassResponse> issueVisitPass(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VisitPassIssueRequest request
    ) {
        return ApiResponse.onSuccess(
                VisitPassSuccessCode.VISIT_PASS_ISSUE_SUCCESS,
                visitPassService.issueVisitPass(userId, request)
        );
    }

    @Operation(summary = "방문 패스 목록 조회", description = "로그인한 사용자의 Visit Pass 목록과 QR 이미지 URL을 조회합니다.")
    @GetMapping
    public ApiResponse<VisitPassListResponse> getVisitPasses(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                VisitPassSuccessCode.VISIT_PASS_LIST_SUCCESS,
                visitPassService.getVisitPasses(userId)
        );
    }
}
