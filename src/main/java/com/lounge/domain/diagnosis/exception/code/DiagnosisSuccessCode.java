package com.lounge.domain.diagnosis.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DiagnosisSuccessCode implements BaseSuccessCode {

    DIAGNOSIS_COMPLETE_SUCCESS(
            HttpStatus.CREATED,
            "DIAGNOSIS_201_1",
            "진단 완료 및 제품 추천에 성공했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
