package com.lounge.domain.recommendationproduct.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecommendationProductErrorCode implements BaseErrorCode {

    RECOMMENDATION_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION_PRODUCT_404_1", "추천 상품을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
