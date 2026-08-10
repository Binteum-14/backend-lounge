package com.lounge.domain.auth.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "AUTH_409_1", "이미 사용 중인 username입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "username 또는 password가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "유효하지 않은 refresh token입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
