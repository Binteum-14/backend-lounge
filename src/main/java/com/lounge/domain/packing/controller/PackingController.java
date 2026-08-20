package com.lounge.domain.packing.controller;

import com.lounge.domain.packing.dto.PackingCheckRequest;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import com.lounge.domain.packing.dto.PackingItemDefinition;
import com.lounge.domain.packing.dto.PackingProfile;
import com.lounge.domain.packing.service.PackingService;
import com.lounge.domain.packing.service.PackingXrayPreviewRenderer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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

    private final PackingXrayPreviewRenderer packingXrayPreviewRenderer;

    @Operation(
            summary = "수납 가능 소지품 목록 조회",
            description = "AI 수납 시뮬레이션에서 선택할 수 있는 대표 소지품 목록을 조회합니다."
    )
    @GetMapping("/items")
    public List<PackingItemDefinition> getItems() {

        return packingService.getAvailableItems();
    }

    @Operation(
            summary = "수납 분석 지원 가방 목록 조회",
            description = "현재 AI 수납 분석을 지원하는 라운지 L01~L07 및 비행기 F01~F07 가방 프로필을 조회합니다. imageUrl은 바로 표시할 수 있는 이미지 경로입니다."
    )
    @GetMapping("/profiles")
    public List<PackingProfile> getProfiles() {

        return packingService.getAvailableProfiles();
    }

    @Operation(
            summary = "라운지 가방 수납 가능성 분석",
            description = """
                    L01~L07 라운지 가방 ID와 사용자가 선택한 소지품을 기준으로
                    공식 제품 치수, 기기 수납 지원 여부, 전체 예상 공간 사용률을 계산합니다.
                    결과에는 수납 적합도와 프론트 시각화용 배치 좌표가 포함됩니다.
                    """
    )
    @PostMapping("/lounge/{loungeId}/check")
    public PackingCheckResponse checkPacking(

            @PathVariable String loungeId,

            @Valid
            @RequestBody
            PackingCheckRequest request
    ) {

        return packingService.check(
                loungeId,
                request
        );
    }

    @Operation(
            summary = "AI 추천 구성 엑스레이 미리보기 생성",
            description = "가방 크기에 맞춰 실제로 수납 가능한 대표 소지품을 추천 구성으로 채운 SVG 이미지를 반환합니다."
    )
    @PostMapping(
            value = "/lounge/{loungeId}/recommended-xray-preview",
            produces = "image/svg+xml"
    )
    public ResponseEntity<String> getRecommendedXrayPreview(

            @PathVariable String loungeId
    ) {

        PackingCheckResponse response = packingService.recommend(
                loungeId
        );

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(packingXrayPreviewRenderer.render(response));
    }

    @Operation(
            summary = "AI 엑스레이 수납 미리보기 생성",
            description = "선택한 소지품의 실제 수납 분석 결과를 가방 안 배치로 표현한 SVG 이미지를 반환합니다."
    )
    @PostMapping(
            value = "/lounge/{loungeId}/xray-preview",
            produces = "image/svg+xml"
    )
    public ResponseEntity<String> getXrayPreview(

            @PathVariable String loungeId,

            @Valid
            @RequestBody
            PackingCheckRequest request
    ) {

        PackingCheckResponse response = packingService.check(
                loungeId,
                request
        );

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(packingXrayPreviewRenderer.render(response));
    }
}
