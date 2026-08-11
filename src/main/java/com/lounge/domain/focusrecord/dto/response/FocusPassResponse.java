package com.lounge.domain.focusrecord.dto.response;

import com.lounge.domain.flightfocusdetail.entity.FlightFocusDetail;
import com.lounge.domain.focusrecord.entity.FocusRecord;
import com.lounge.domain.focusrecord.entity.FocusThemeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FocusPassResponse {

    private Long focusRecordId;
    private FocusThemeType themeType;
    private Integer allMinutes;
    private Integer studySeconds;
    private Integer breakSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;

    public static FocusPassResponse from(FocusRecord focusRecord) {
        FlightFocusDetail flightFocusDetail = focusRecord.getFlightFocusDetail();

        return new FocusPassResponse(
                focusRecord.getId(),
                focusRecord.getThemeType(),
                focusRecord.getAllMinutes(),
                focusRecord.getStudySeconds(),
                focusRecord.getBreakSeconds(),
                focusRecord.getStartedAt(),
                focusRecord.getEndedAt(),
                flightFocusDetail == null ? null : flightFocusDetail.getFlightNumber(),
                flightFocusDetail == null ? null : flightFocusDetail.getDepartureAirport(),
                flightFocusDetail == null ? null : flightFocusDetail.getArrivalAirport(),
                flightFocusDetail == null ? null : flightFocusDetail.getDepartureAt(),
                flightFocusDetail == null ? null : flightFocusDetail.getArrivalAt()
        );
    }
}
