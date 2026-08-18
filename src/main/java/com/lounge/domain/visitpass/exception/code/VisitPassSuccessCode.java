package com.lounge.domain.visitpass.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VisitPassSuccessCode implements BaseSuccessCode {

    VISIT_PASS_ISSUE_SUCCESS(HttpStatus.CREATED, "VISIT_PASS_201_1", "방문 패스 발급에 성공했습니다."),
    VISIT_PASS_LIST_SUCCESS(HttpStatus.OK, "VISIT_PASS_200_1", "방문 패스 목록 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
