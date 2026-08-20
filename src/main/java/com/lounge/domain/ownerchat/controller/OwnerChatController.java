package com.lounge.domain.ownerchat.controller;

import com.lounge.domain.ownerchat.dto.request.OwnerChatRequest;
import com.lounge.domain.ownerchat.dto.response.OwnerChatResponse;
import com.lounge.domain.ownerchat.exception.code.OwnerChatSuccessCode;
import com.lounge.domain.ownerchat.service.OwnerChatService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Owner Lounge",
        description = "로그인 사용자를 위한 MCM 제품 관리 AI 채팅 API"
)
@Validated
@RestController
@RequestMapping("/api/owner-lounge")
@RequiredArgsConstructor
public class OwnerChatController {

    private final OwnerChatService ownerChatService;

    @Operation(
            summary = "AI 제품 관리 질문",
            description = """
                    질문에 포함된 MCM 제품명을 제품 DB와 대조합니다.
                    제품명이 없거나 제품이 여러 개로 식별되면 정확한 제품명을 다시 요청하고,
                    하나의 제품으로 확정된 경우에만 해당 제품의 관리 정보와 특징을 바탕으로 AI 답변을 생성합니다.
                    서버에는 채팅 메시지를 저장하지 않으므로 프론트는 최근 대화를 history 배열로 함께 보내야 합니다.
                    """
    )
    @PostMapping("/chat")
    public ApiResponse<OwnerChatResponse> chat(
            @Valid @RequestBody OwnerChatRequest request
    ) {

        return ApiResponse.onSuccess(
                OwnerChatSuccessCode.CHAT_REPLY_SUCCESS,
                ownerChatService.chat(request)
        );
    }
}