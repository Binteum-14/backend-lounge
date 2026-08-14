package com.lounge.domain.visitpass.controller;

import com.lounge.domain.visitpass.service.VisitPassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "VisitPass", description = "방문 패스 API")
@RestController
@RequestMapping("/api/visitpass")
@RequiredArgsConstructor
public class VisitPassController {

    private final VisitPassService visitPassService;
}
