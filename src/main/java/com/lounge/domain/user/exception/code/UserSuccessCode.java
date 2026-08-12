package com.lounge.domain.user.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserSuccessCode implements BaseSuccessCode {

    USER_ME_SUCCESS(HttpStatus.OK, "USER_200_1", "내 정보 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
