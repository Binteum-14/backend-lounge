package com.lounge.domain.focusrecord.controller;

import com.lounge.domain.focusrecord.dto.request.FocusPassSaveRequest;
import com.lounge.domain.focusrecord.dto.response.FocusPassSaveResponse;
import com.lounge.domain.focusrecord.exception.code.FocusRecordSuccessCode;
import com.lounge.domain.focusrecord.service.FocusRecordService;
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

@Tag(name = "FocusRecord", description = "집중 기록 API")
@RestController
@RequestMapping("/api/focus")
@RequiredArgsConstructor
public class FocusRecordController {

    private final FocusRecordService focusRecordService;

    @Operation(
            summary = "포커스 패스 저장",
            description = "인증된 사용자의 포커스 패스를 저장합니다. themeType은 LOUNGE 또는 FLIGHT만 가능하며, FLIGHT일 때 flight 정보가 필요합니다."
    )
    @PostMapping("/pass")
    public ApiResponse<FocusPassSaveResponse> saveFocusPass(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FocusPassSaveRequest request
    ) {
        return ApiResponse.onSuccess(
                FocusRecordSuccessCode.FOCUS_PASS_SAVE_SUCCESS,
                focusRecordService.saveFocusPass(userId, request)
        );
    }
}
