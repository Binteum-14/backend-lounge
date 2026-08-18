package com.lounge.domain.diagnosis.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DiagnosisErrorCode implements BaseErrorCode {

    DIAGNOSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "DIAGNOSIS_404_1", "진단을 찾을 수 없습니다."),
    INVALID_ANSWERS(HttpStatus.BAD_REQUEST, "DIAGNOSIS_400_1", "진단 답변 7개를 확인해주세요."),
    UNKNOWN_ANSWER(HttpStatus.BAD_REQUEST, "DIAGNOSIS_400_2", "알 수 없는 문항 또는 답변 코드입니다."),
    INSUFFICIENT_PRODUCTS(HttpStatus.UNPROCESSABLE_CONTENT, "DIAGNOSIS_422_1", "추천할 수 있는 제품이 부족합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
