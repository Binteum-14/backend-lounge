package com.lounge.domain.visitpass.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VisitPassErrorCode implements BaseErrorCode {

    VISIT_PASS_NOT_FOUND(HttpStatus.NOT_FOUND, "VISIT_PASS_404_1", "방문 패스를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
