package com.lounge.domain.packing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A verified packing profile for one lounge bag.
 *
 * <p>Instances are loaded from {@code packing/mcm-packing-profiles.json}.
 * Dimensions use centimetres to match the source data.</p>
 */
public record PackingProfile(

        String loungeId,

        String scene,

        String sku,

        String productName,

        String color,

        String imagePath,

        Double depthCm,

        Double widthCm,

        Double heightCm,

        Boolean laptopSupported,

        Double laptopMaxInches,

        Boolean tabletSupported,

        String sourceType,

        String sourceUrl
) {

    public double volumeCm3() {
        return widthCm * heightCm * depthCm;
    }

    /**
     * {@code loungeId}라는 옛 내부 이름을 호환성 때문에 유지한다.
     * API 응답에서는 이 필드를 사용해야 한다.
     */
    @JsonProperty("packingProfileId")
    public String getPackingProfileId() {
        return loungeId;
    }

    @JsonProperty("imageUrl")
    public String getImageUrl() {
        String staticPath = imagePath.startsWith("가방/")
                ? imagePath.substring("가방/".length())
                : imagePath;
        return "/packing-assets/" + staticPath;
    }
}
