package com.lounge.domain.flightfocusdetail.controller;

import com.lounge.domain.flightfocusdetail.service.FlightFocusDetailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FlightFocusDetail", description = "항공 집중 상세 API")
@RestController
@RequestMapping("/api/flightfocusdetail")
@RequiredArgsConstructor
public class FlightFocusDetailController {

    private final FlightFocusDetailService flightFocusDetailService;
}
