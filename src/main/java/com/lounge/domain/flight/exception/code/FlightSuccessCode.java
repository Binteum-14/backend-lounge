package com.lounge.domain.flight.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FlightSuccessCode implements BaseSuccessCode {

    FLIGHT_RECOMMEND_SUCCESS(HttpStatus.OK, "FLIGHT_200_1", "항공편 추천 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
