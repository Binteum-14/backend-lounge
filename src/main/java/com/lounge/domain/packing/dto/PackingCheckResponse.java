package com.lounge.domain.packing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lounge.domain.packing.PackingStatus;

import java.util.List;

public record PackingCheckResponse(

        String loungeId,

        String scene,

        String sku,

        String productName,

        String color,

        String imagePath,

        String imageUrl,

        PackingStatus status,

        int fitScore,

        double usedSpaceRatio,

        BagSize bagSize,

        List<ItemResult> items,

        List<Placement> placements,

        String notice,

        String sourceUrl
) {

    /**
     * 메뉴 상세 응답의 packingProfileId와 같은 값입니다.
     * loungeId는 예전 응답 호환성을 위해 함께 유지합니다.
     */
    @JsonProperty("packingProfileId")
    public String getPackingProfileId() {
        return loungeId;
    }

    public record BagSize(
            double widthMm,
            double heightMm,
            double depthMm
    ) {
    }

    public record ItemResult(
            String itemCode,
            String itemName,
            boolean fit,
            boolean tight,
            double widthMm,
            double heightMm,
            double depthMm,
            String reason
    ) {
    }

    /**
     * 프론트에서 수납 이미지를 그릴 때 사용하는 상대 좌표.
     *
     * 모든 값은 0 ~ 100 사이의 퍼센트 값이다.
     *
     * 예:
     * x = 10
     * y = 20
     * width = 40
     * height = 30
     */
    public record Placement(
            String itemCode,
            String itemName,
            double x,
            double y,
            double width,
            double height,
            double rotation
    ) {
    }
}
