package com.lounge.domain.recommendation.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecommendationSuccessCode
        implements BaseSuccessCode {

    RECOMMENDATION_GENERATE_SUCCESS(
            HttpStatus.CREATED,
            "RECOMMENDATION_201_1",
            "AI 제품 추천 생성에 성공했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}