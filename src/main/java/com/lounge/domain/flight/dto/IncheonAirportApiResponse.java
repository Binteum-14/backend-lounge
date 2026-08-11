package com.lounge.domain.flight.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncheonAirportApiResponse {

    private String airline;
    private String flightId;
    private String scheduleDateTime;
    private String estimatedDateTime;
    private String airport;
    private String airportCode;
    private String terminalId;
    private String remark;
    private String elapsetime;
    private String codeshare;
    private String masterflightid;
}
