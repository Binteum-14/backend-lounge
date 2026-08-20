package com.lounge.domain.ownerchat.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OwnerChatSuccessCode implements BaseSuccessCode {

    OWNED_PRODUCT_LIST_SUCCESS(
            HttpStatus.OK,
            "OWNER_CHAT_200_1",
            "보유 제품 목록 조회에 성공했습니다."
    ),

    CHAT_CONTEXT_SUCCESS(
            HttpStatus.OK,
            "OWNER_CHAT_200_2",
            "제품 관리 채팅 화면 정보 조회에 성공했습니다."
    ),

    CHAT_REPLY_SUCCESS(
            HttpStatus.OK,
            "OWNER_CHAT_200_3",
            "AI 제품 관리 답변 생성에 성공했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
