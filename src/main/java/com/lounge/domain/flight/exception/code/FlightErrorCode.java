package com.lounge.domain.flight.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FlightErrorCode implements BaseErrorCode {

    FLIGHT_API_ERROR(HttpStatus.BAD_GATEWAY, "FLIGHT_502_1", "항공편 공공데이터 API 호출에 실패했습니다."),
    FLIGHT_CACHE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FLIGHT_500_1", "항공편 캐시 처리에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
