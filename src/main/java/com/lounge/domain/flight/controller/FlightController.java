package com.lounge.domain.flight.controller;

import com.lounge.domain.flight.dto.FlightRecommendResponse;
import com.lounge.domain.flight.exception.code.FlightSuccessCode;
import com.lounge.domain.flight.service.FlightService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;

@Tag(name = "Flight", description = "항공편 추천 API")
@Validated
@RestController
@RequestMapping("/api/focus/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "항공편 추천", description = "오늘 인천공항 출발편 중 시작 시간과 집중 시간에 가까운 항공편을 추천합니다.")
    @GetMapping
    public ApiResponse<List<FlightRecommendResponse>> recommendFlights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @Min(value = 1, message = "focusMinutes는 1 이상이어야 합니다.")
            @RequestParam int focusMinutes
    ) {
        return ApiResponse.onSuccess(
                FlightSuccessCode.FLIGHT_RECOMMEND_SUCCESS,
                flightService.recommendFlights(startTime, focusMinutes)
        );
    }
}
