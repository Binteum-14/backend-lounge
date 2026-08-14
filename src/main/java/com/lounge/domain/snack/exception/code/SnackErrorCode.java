package com.lounge.domain.snack.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SnackErrorCode implements BaseErrorCode {

    SNACK_NOT_FOUND(HttpStatus.NOT_FOUND, "SNACK_404_1", "간식을 찾을 수 없습니다."),
    SNACK_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "SNACK_404_2", "간식과 연결된 상품 구성을 찾을 수 없습니다."),
    SNACK_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "SNACK_404_3", "간식과 연결된 상품을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
