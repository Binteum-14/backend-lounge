package com.lounge.domain.packing.dto;

public record PackingItemDefinition(
        String code,
        String name,
        String category,
        double widthMm,
        double heightMm,
        double depthMm,
        String referenceNote
) {
    public double volume() {
        return widthMm * heightMm * depthMm;
    }
}