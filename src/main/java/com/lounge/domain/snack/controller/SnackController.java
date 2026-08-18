package com.lounge.domain.snack.controller;

import com.lounge.domain.snack.dto.response.SnackDetailResponse;
import com.lounge.domain.snack.dto.response.SnackResponse;
import com.lounge.domain.snack.exception.code.SnackSuccessCode;
import com.lounge.domain.snack.service.SnackService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Snack", description = "Focus 간식 API")
@RestController
@RequestMapping("/api/focus/snacks")
@RequiredArgsConstructor
public class SnackController {

    private final SnackService snackService;

    @Operation(summary = "간식 목록 조회", description = "Focus Lounge에서 선택 가능한 활성 간식 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<SnackResponse>> getSnacks() {
        return ApiResponse.onSuccess(SnackSuccessCode.SNACK_LIST_SUCCESS, snackService.getSnacks());
    }

    @Operation(summary = "간식 주문하기", description = "선택한 간식과 연결된 제품 variant ID, 간식/제품 이미지 URL을 함께 조회합니다.")
    @GetMapping("/{snackId}")
    public ApiResponse<SnackDetailResponse> getSnack(
            @PathVariable Long snackId
    ) {
        return ApiResponse.onSuccess(SnackSuccessCode.SNACK_DETAIL_SUCCESS, snackService.getSnack(snackId));
    }
}
