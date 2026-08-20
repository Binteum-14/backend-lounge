package com.lounge.domain.ownerchat.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OwnerChatErrorCode implements BaseErrorCode {

    OWNED_PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "OWNER_CHAT_404_1",
            "보유 제품을 찾을 수 없습니다."
    ),

    INVALID_HISTORY(
            HttpStatus.BAD_REQUEST,
            "OWNER_CHAT_400_1",
            "대화 기록의 순서와 내용을 확인해주세요."
    ),

    OPENAI_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "OWNER_CHAT_502_1",
            "AI 제품 관리 답변 생성 중 오류가 발생했습니다."
    ),

    OPENAI_INVALID_RESPONSE(
            HttpStatus.BAD_GATEWAY,
            "OWNER_CHAT_502_2",
            "AI 제품 관리 답변 형식이 올바르지 않습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
