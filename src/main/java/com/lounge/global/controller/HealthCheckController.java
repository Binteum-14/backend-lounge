package com.lounge.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "health", description = "health check api")
@RestController
public class HealthCheckController {

    @Operation(summary = "헬스 체크 API", description = "서버가 정상적으로 실행 중인지 확인합니다.")
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}