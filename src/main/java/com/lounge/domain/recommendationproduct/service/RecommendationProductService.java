package com.lounge.domain.recommendationproduct.service;

import com.lounge.domain.recommendationproduct.repository.RecommendationProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationProductService {

    private final RecommendationProductRepository recommendationProductRepository;
}
