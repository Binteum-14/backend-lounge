package com.lounge.domain.flightfocusdetail.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FlightFocusDetailErrorCode implements BaseErrorCode {

    FLIGHT_FOCUS_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "FLIGHT_FOCUS_DETAIL_404_1", "항공 집중 상세를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
