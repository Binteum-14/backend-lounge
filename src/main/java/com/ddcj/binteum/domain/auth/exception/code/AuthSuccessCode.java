package com.ddcj.binteum.domain.auth.exception.code;

import com.ddcj.binteum.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH_201_1", "회원가입에 성공했습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_1", "로그인에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
