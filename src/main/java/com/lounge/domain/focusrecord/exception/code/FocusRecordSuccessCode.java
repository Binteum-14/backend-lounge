package com.lounge.domain.focusrecord.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FocusRecordSuccessCode implements BaseSuccessCode {

    FOCUS_PASS_SAVE_SUCCESS(HttpStatus.CREATED, "FOCUS_RECORD_201_1", "포커스 패스 저장에 성공했습니다."),
    FOCUS_PASS_LIST_SUCCESS(HttpStatus.OK, "FOCUS_RECORD_200_1", "포커스 패스 목록 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
