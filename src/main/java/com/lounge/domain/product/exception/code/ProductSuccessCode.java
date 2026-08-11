package com.lounge.domain.product.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductSuccessCode implements BaseSuccessCode {

    PRODUCT_LIST_SUCCESS(HttpStatus.OK, "PRODUCT_200_1", "상품 목록 조회에 성공했습니다."),
    PRODUCT_DETAIL_SUCCESS(HttpStatus.OK, "PRODUCT_200_2", "상품 상세 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
