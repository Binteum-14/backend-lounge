package com.lounge.domain.flightfocusdetail.service;

import com.lounge.domain.flightfocusdetail.repository.FlightFocusDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightFocusDetailService {

    private final FlightFocusDetailRepository flightFocusDetailRepository;
}
