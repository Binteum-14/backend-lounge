package com.lounge.domain.snack.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SnackSuccessCode implements BaseSuccessCode {

    SNACK_LIST_SUCCESS(HttpStatus.OK, "SNACK_200_1", "간식 목록 조회에 성공했습니다."),
    SNACK_DETAIL_SUCCESS(HttpStatus.OK, "SNACK_200_2", "간식 주문하기를 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
