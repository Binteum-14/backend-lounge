package com.lounge.domain.focusrecord.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FocusRecordErrorCode implements BaseErrorCode {

    FOCUS_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "FOCUS_RECORD_404_1", "집중 기록을 찾을 수 없습니다."),
    FLIGHT_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "FOCUS_RECORD_400_1", "비행기 테마는 항공편 정보가 필요합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
