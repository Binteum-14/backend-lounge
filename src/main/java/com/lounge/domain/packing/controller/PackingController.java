package com.lounge.domain.packing.controller;

import com.lounge.domain.packing.dto.PackingCheckRequest;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import com.lounge.domain.packing.dto.PackingItemDefinition;
import com.lounge.domain.packing.service.PackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "AI Packing",
        description = "MCM 제품 AI 수납 시뮬레이션 API"
)
@RestController
@RequestMapping("/api/packing")
@RequiredArgsConstructor
public class PackingController {

    private final PackingService packingService;

    @Operation(
            summary = "수납 가능 소지품 목록 조회",
            description = "AI 수납 시뮬레이션에서 선택할 수 있는 대표 소지품 목록을 조회합니다."
    )
    @GetMapping("/items")
    public List<PackingItemDefinition> getItems() {

        return packingService.getAvailableItems();
    }

    @Operation(
            summary = "제품 수납 가능성 분석",
            description = """
                    제품 ID와 사용자가 선택한 소지품을 기준으로
                    제품 크기, 개별 물건 크기, 전체 예상 공간 사용률을 계산합니다.
                    결과에는 수납 적합도와 프론트 시각화용 배치 좌표가 포함됩니다.
                    """
    )
    @PostMapping("/products/{productId}/check")
    public PackingCheckResponse checkPacking(

            @PathVariable Long productId,

            @Valid
            @RequestBody
            PackingCheckRequest request
    ) {

        return packingService.check(
                productId,
                request
        );
    }
}