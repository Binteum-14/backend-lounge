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

    @JsonProperty("imageUrl")
    public String getImageUrl() {
        String staticPath = imagePath.startsWith("가방/")
                ? imagePath.substring("가방/".length())
                : imagePath;
        return "/packing-assets/" + staticPath;
    }
}
