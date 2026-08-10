package com.lounge.domain.auth.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    USERNAME_CHECK_SUCCESS(HttpStatus.OK, "AUTH_200_0", "username 중복 확인에 성공했습니다."),
    SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH_201_1", "회원가입에 성공했습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_1", "로그인에 성공했습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AUTH_200_2", "토큰 재발급에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_3", "로그아웃에 성공했습니다."),
    WITHDRAW_SUCCESS(HttpStatus.OK, "AUTH_200_4", "회원 탈퇴에 성공했습니다."),
    GUEST_SESSION_ISSUED_SUCCESS(HttpStatus.OK, "AUTH_200_5", "게스트 세션 발급에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
