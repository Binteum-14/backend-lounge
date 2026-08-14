package com.lounge.domain.visitpassproduct.controller;

import com.lounge.domain.visitpassproduct.service.VisitPassProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "VisitPassProduct", description = "방문 패스 상품 API")
@RestController
@RequestMapping("/api/visitpassproduct")
@RequiredArgsConstructor
public class VisitPassProductController {

    private final VisitPassProductService visitPassProductService;
}
