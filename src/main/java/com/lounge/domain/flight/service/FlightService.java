package com.lounge.domain.flight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.domain.flight.AirportLogoCatalog;
import com.lounge.domain.flight.client.IncheonAirportApiClient;
import com.lounge.domain.flight.dto.FlightRecommendResponse;
import com.lounge.domain.flight.dto.IncheonAirportApiResponse;
import com.lounge.domain.flight.exception.code.FlightErrorCode;
import com.lounge.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService {

    private static final String FLIGHT_DEPARTURE_KEY_PREFIX = "flight:departure:";
    private static final int DEPARTURE_TIME_TOLERANCE_MINUTES = 60;
    private static final int DURATION_TOLERANCE_MINUTES = 30;
    private static final int RECOMMENDATION_LIMIT = 5;

    private final IncheonAirportApiClient incheonAirportApiClient;
    private final FlightDurationEstimator flightDurationEstimator;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${incheon-airport.api.cache-ttl}")
    private Long cacheTtl;

    public List<FlightRecommendResponse> recommendFlights(LocalTime startTime, int focusMinutes) {
        return getTodayDepartures().stream()
                .map(this::toCandidate)
                .flatMap(Optional::stream)
                .filter(candidate -> candidate.isWithin(startTime, focusMinutes))
                .sorted(Comparator
                        .comparingInt((FlightCandidate candidate) -> candidate.durationDifference(focusMinutes))
                        .thenComparingInt(candidate -> candidate.departureTimeDifference(startTime)))
                .limit(RECOMMENDATION_LIMIT)
                .map(this::toResponse)
                .toList();
    }

    private FlightRecommendResponse toResponse(FlightCandidate candidate) {
        return FlightRecommendResponse.of(
                candidate.flightNumber,
                candidate.airline,
                candidate.arrivalAirportCode,
                candidate.arrivalAirportName,
                resolveAirportImageUrl(candidate.arrivalAirportCode),
                candidate.departureTime,
                candidate.durationMinutes
        );
    }

    private String resolveAirportImageUrl(String airportCode) {
        return AirportLogoCatalog.findImageUrl(airportCode).orElse(null);
    }

    private List<IncheonAirportApiResponse> getTodayDepartures() {
        String key = createCacheKey();
        String cachedDepartures = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(cachedDepartures)) {
            return readCache(cachedDepartures);
        }

        List<IncheonAirportApiResponse> departures = incheonAirportApiClient.fetchTodayDepartures();
        writeCache(key, departures);
        return departures;
    }

    private List<IncheonAirportApiResponse> readCache(String cachedDepartures) {
        try {
            return objectMapper.readValue(cachedDepartures, new TypeReference<List<IncheonAirportApiResponse>>() {
            });
        } catch (JsonProcessingException exception) {
            throw GeneralException.of(FlightErrorCode.FLIGHT_CACHE_ERROR);
        }
    }

    private void writeCache(String key, List<IncheonAirportApiResponse> departures) {
        try {
            stringRedisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(departures),
                    Duration.ofMillis(cacheTtl)
            );
        } catch (JsonProcessingException exception) {
            throw GeneralException.of(FlightErrorCode.FLIGHT_CACHE_ERROR);
        }
    }

    private Optional<FlightCandidate> toCandidate(IncheonAirportApiResponse item) {
        Optional<LocalTime> departureTime = parseTime(item.getScheduleDateTime());
        Optional<Integer> durationMinutes = resolveDurationMinutes(item);

        if (departureTime.isEmpty() || durationMinutes.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new FlightCandidate(
                item.getFlightId(),
                item.getAirline(),
                item.getAirportCode(),
                item.getAirport(),
                departureTime.get(),
                durationMinutes.get()
        ));
    }

    private Optional<Integer> resolveDurationMinutes(IncheonAirportApiResponse item) {
        Optional<Integer> elapsedTime = parseElapsedTime(item.getElapsetime());
        if (elapsedTime.isPresent()) {
            return elapsedTime;
        }

        return flightDurationEstimator.estimateFromIncheon(item.getAirportCode());
    }

    private Optional<LocalTime> parseTime(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }

        String time = value.trim();
        if (time.length() >= 4) {
            time = time.substring(time.length() - 4);
        }

        try {
            int hour = Integer.parseInt(time.substring(0, 2));
            int minute = Integer.parseInt(time.substring(2, 4));
            return Optional.of(LocalTime.of(hour, minute));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Optional<Integer> parseElapsedTime(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }

        String elapsedTime = value.trim();
        if (elapsedTime.length() < 4) {
            return Optional.empty();
        }

        try {
            int hours = Integer.parseInt(elapsedTime.substring(0, 2));
            int minutes = Integer.parseInt(elapsedTime.substring(2, 4));
            int totalMinutes = hours * 60 + minutes;
            if (totalMinutes <= 0) {
                return Optional.empty();
            }
            return Optional.of(totalMinutes);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String createCacheKey() {
        return FLIGHT_DEPARTURE_KEY_PREFIX + LocalDate.now();
    }

    private static class FlightCandidate {

        private final String flightNumber;
        private final String airline;
        private final String arrivalAirportCode;
        private final String arrivalAirportName;
        private final LocalTime departureTime;
        private final Integer durationMinutes;

        private FlightCandidate(
                String flightNumber,
                String airline,
                String arrivalAirportCode,
                String arrivalAirportName,
                LocalTime departureTime,
                Integer durationMinutes
        ) {
            this.flightNumber = flightNumber;
            this.airline = airline;
            this.arrivalAirportCode = arrivalAirportCode;
            this.arrivalAirportName = arrivalAirportName;
            this.departureTime = departureTime;
            this.durationMinutes = durationMinutes;
        }

        private boolean isWithin(LocalTime requestedStartTime, int requestedDurationMinutes) {
            return departureTimeDifference(requestedStartTime) <= DEPARTURE_TIME_TOLERANCE_MINUTES
                    && durationDifference(requestedDurationMinutes) <= DURATION_TOLERANCE_MINUTES;
        }

        private int departureTimeDifference(LocalTime requestedStartTime) {
            int difference = Math.abs((int) Duration.between(requestedStartTime, departureTime).toMinutes());
            return Math.min(difference, 24 * 60 - difference);
        }

        private int durationDifference(int requestedDurationMinutes) {
            return Math.abs(durationMinutes - requestedDurationMinutes);
        }
    }
}
