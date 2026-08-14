package com.lounge.domain.flight.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.domain.flight.dto.IncheonAirportApiResponse;
import com.lounge.domain.flight.exception.code.FlightErrorCode;
import com.lounge.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IncheonAirportApiClient {

    private static final String DEPARTURE_ENDPOINT = "/getPassengerDeparturesOdp";
    private static final String NORMAL_RESULT_CODE = "00";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${incheon-airport.api.base-url}")
    private String baseUrl;

    @Value("${incheon-airport.api.service-key}")
    private String serviceKey;

    public List<IncheonAirportApiResponse> fetchTodayDepartures() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(createDepartureUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw GeneralException.of(FlightErrorCode.FLIGHT_API_ERROR);
            }

            return parseItems(response.body());
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw GeneralException.of(FlightErrorCode.FLIGHT_API_ERROR);
        }
    }

    private String createDepartureUrl() {
        return UriComponentsBuilder.fromUriString(baseUrl + DEPARTURE_ENDPOINT)
                .queryParam("serviceKey", serviceKey)
                .queryParam("from_time", "0000")
                .queryParam("to_time", "2400")
                .queryParam("lang", "K")
                .queryParam("type", "json")
                .build(false)
                .toUriString();
    }

    private List<IncheonAirportApiResponse> parseItems(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!NORMAL_RESULT_CODE.equals(resultCode)) {
                throw GeneralException.of(FlightErrorCode.FLIGHT_API_ERROR);
            }

            JsonNode itemsNode = root.path("response").path("body").path("items");
            if (itemsNode.isMissingNode() || itemsNode.isNull() || itemsNode.isTextual()) {
                return List.of();
            }

            JsonNode itemNode = itemsNode.has("item") ? itemsNode.get("item") : itemsNode;
            return parseItemNode(itemNode);
        } catch (JsonProcessingException exception) {
            throw GeneralException.of(FlightErrorCode.FLIGHT_API_ERROR);
        }
    }

    private List<IncheonAirportApiResponse> parseItemNode(JsonNode itemNode) throws JsonProcessingException {
        List<IncheonAirportApiResponse> items = new ArrayList<>();

        if (itemNode.isArray()) {
            for (JsonNode item : itemNode) {
                items.add(objectMapper.treeToValue(item, IncheonAirportApiResponse.class));
            }
            return items;
        }

        if (itemNode.isObject()) {
            items.add(objectMapper.treeToValue(itemNode, IncheonAirportApiResponse.class));
        }

        return items;
    }
}
