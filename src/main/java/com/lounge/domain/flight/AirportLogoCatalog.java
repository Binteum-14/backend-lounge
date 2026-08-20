package com.lounge.domain.flight;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

public final class AirportLogoCatalog {

    private static final String BASE_URL =
            "https://lounge-backend-bucket.s3.ap-northeast-2.amazonaws.com/";

    private static final Map<String, String> OBJECT_KEYS = Map.of(
            "ICN", "airports/ICN.png",
            "NRT", "airports/NRT.png",
            "HND", "airports/HND.jpg",
            "KIX", "airports/KIX.png",
            "FUK", "airports/FUK.jpeg",
            "TPE", "airports/TPE.png",
            "HKG", "airports/HKG.png"
    );

    private AirportLogoCatalog() {
    }

    public static Optional<String> findImageUrl(String airportCode) {
        if (!StringUtils.hasText(airportCode)) {
            return Optional.empty();
        }

        return Optional.ofNullable(OBJECT_KEYS.get(airportCode.trim().toUpperCase()))
                .map(objectKey -> BASE_URL + objectKey);
    }
}
