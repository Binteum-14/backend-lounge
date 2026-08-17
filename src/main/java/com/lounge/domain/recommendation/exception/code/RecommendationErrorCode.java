package com.lounge.domain.recommendation.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecommendationErrorCode implements BaseErrorCode {

    RECOMMENDATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECOMMENDATION_404_1",
            "추천을 찾을 수 없습니다."
    ),

    OPENAI_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "RECOMMENDATION_502_1",
            "AI 추천 설명 생성 중 오류가 발생했습니다."
    ),

    OPENAI_INVALID_RESPONSE(
            HttpStatus.BAD_GATEWAY,
            "RECOMMENDATION_502_2",
            "AI 추천 결과 형식이 올바르지 않습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}