package com.lounge.domain.visitpassproduct.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VisitPassProductErrorCode implements BaseErrorCode {

    VISIT_PASS_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "VISIT_PASS_PRODUCT_404_1", "방문 패스 상품을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
