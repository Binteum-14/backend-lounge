package com.lounge.domain.diagnosisanswer.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DiagnosisAnswerErrorCode implements BaseErrorCode {

    DIAGNOSIS_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "DIAGNOSIS_ANSWER_404_1", "진단 답변을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
