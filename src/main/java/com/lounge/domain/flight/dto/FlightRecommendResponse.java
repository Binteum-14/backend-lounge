package com.lounge.domain.flight.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FlightRecommendResponse {

    private String flightNumber;
    private String airline;
    private String departureAirportCode;
    private String departureAirportName;
    private String arrivalAirportCode;
    private String arrivalAirportName;
    private LocalTime departureTime;
    private Integer durationMinutes;

    public static FlightRecommendResponse of(
            String flightNumber,
            String airline,
            String arrivalAirportCode,
            String arrivalAirportName,
            LocalTime departureTime,
            Integer durationMinutes
    ) {
        return new FlightRecommendResponse(
                flightNumber,
                airline,
                "ICN",
                "인천",
                arrivalAirportCode,
                arrivalAirportName,
                departureTime,
                durationMinutes
        );
    }
}
