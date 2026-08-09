package com.lounge.domain.recommendationproduct.controller;

import com.lounge.domain.recommendationproduct.service.RecommendationProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RecommendationProduct", description = "추천 상품 API")
@RestController
@RequestMapping("/api/recommendationproduct")
@RequiredArgsConstructor
public class RecommendationProductController {

    private final RecommendationProductService recommendationProductService;
}
