package com.lounge.domain.flightfocusdetail.entity;

import com.lounge.domain.focusrecord.entity.FocusRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_focus_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightFocusDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "focus_record_id", nullable = false, unique = true)
    private FocusRecord focusRecord;

    @Column(name = "flight_number")
    private String flightNumber;

    @Column(name = "departure_airport")
    private String departureAirport;

    @Column(name = "arrival_airport")
    private String arrivalAirport;

    @Column(name = "departure_at")
    private LocalDateTime departureAt;

    @Column(name = "arrival_at")
    private LocalDateTime arrivalAt;

    private FlightFocusDetail(
            FocusRecord focusRecord,
            String flightNumber,
            String departureAirport,
            String arrivalAirport,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt
    ) {
        this.focusRecord = focusRecord;
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureAt = departureAt;
        this.arrivalAt = arrivalAt;
    }

    public static FlightFocusDetail create(
            FocusRecord focusRecord,
            String flightNumber,
            String departureAirport,
            String arrivalAirport,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt
    ) {
        return new FlightFocusDetail(
                focusRecord,
                flightNumber,
                departureAirport,
                arrivalAirport,
                departureAt,
                arrivalAt
        );
    }
}
