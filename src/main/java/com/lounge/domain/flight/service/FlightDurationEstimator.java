package com.lounge.domain.flight.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Component
public class FlightDurationEstimator {

    private static final Map<String, Integer> ICN_DEPARTURE_DURATION_MINUTES = Map.ofEntries(
            Map.entry("NRT", 140),
            Map.entry("HND", 140),
            Map.entry("KIX", 110),
            Map.entry("FUK", 80),
            Map.entry("CTS", 160),
            Map.entry("OKA", 140),
            Map.entry("TPE", 160),
            Map.entry("KHH", 180),
            Map.entry("HKG", 230),
            Map.entry("MFM", 230),
            Map.entry("PVG", 130),
            Map.entry("PEK", 140),
            Map.entry("PKX", 140),
            Map.entry("TAO", 90),
            Map.entry("DLC", 90),
            Map.entry("CAN", 230),
            Map.entry("SZX", 230),
            Map.entry("MNL", 250),
            Map.entry("CEB", 270),
            Map.entry("HAN", 280),
            Map.entry("DAD", 290),
            Map.entry("SGN", 320),
            Map.entry("BKK", 360),
            Map.entry("DMK", 360),
            Map.entry("CNX", 360),
            Map.entry("SIN", 390),
            Map.entry("KUL", 400),
            Map.entry("BKI", 310),
            Map.entry("DPS", 430),
            Map.entry("CGK", 430),
            Map.entry("DEL", 470),
            Map.entry("BOM", 540),
            Map.entry("ULN", 220),
            Map.entry("TAS", 450),
            Map.entry("ALA", 410),
            Map.entry("GUM", 260),
            Map.entry("SPN", 260),
            Map.entry("SYD", 620),
            Map.entry("MEL", 650),
            Map.entry("AKL", 690),
            Map.entry("LAX", 660),
            Map.entry("SFO", 630),
            Map.entry("SEA", 600),
            Map.entry("LAS", 680),
            Map.entry("JFK", 840),
            Map.entry("ORD", 800),
            Map.entry("DFW", 780),
            Map.entry("HNL", 480),
            Map.entry("YVR", 590),
            Map.entry("YYZ", 820),
            Map.entry("LHR", 840),
            Map.entry("CDG", 830),
            Map.entry("FRA", 800),
            Map.entry("AMS", 820),
            Map.entry("FCO", 790),
            Map.entry("MAD", 850),
            Map.entry("IST", 720),
            Map.entry("DXB", 600),
            Map.entry("DOH", 610),
            Map.entry("AUH", 600)
    );

    public Optional<Integer> estimateFromIncheon(String airportCode) {
        if (!StringUtils.hasText(airportCode)) {
            return Optional.empty();
        }

        String normalizedAirportCode = airportCode.trim().toUpperCase();
        return Optional.ofNullable(ICN_DEPARTURE_DURATION_MINUTES.get(normalizedAirportCode));
    }
}
