package com.lounge.domain.packing.service;

import com.lounge.domain.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductDimensionExtractor {

    /**
     * 다음과 같은 형식을 탐지한다.
     *
     * 33 x 25 x 13 cm
     * 33.5 x 25.0 x 13.0 cm
     * 33 × 25 × 13 cm
     * 330 x 250 x 130 mm
     */
    private static final Pattern TRIPLE_DIMENSION_PATTERN =
            Pattern.compile(
                    "(?i)" +
                            "(\\d{1,4}(?:\\.\\d+)?)\\s*(?:cm|mm)?\\s*" +
                            "[x×X]\\s*" +
                            "(\\d{1,4}(?:\\.\\d+)?)\\s*(?:cm|mm)?\\s*" +
                            "[x×X]\\s*" +
                            "(\\d{1,4}(?:\\.\\d+)?)\\s*" +
                            "(cm|mm)"
            );

    public Optional<BagDimensions> extract(Product product) {

        if (product == null) {
            return Optional.empty();
        }

        Optional<BagDimensions> fromFeature =
                extractFromText(product.getProductFeature());

        if (fromFeature.isPresent()) {
            return fromFeature;
        }

        return extractFromText(product.getDescription());
    }

    private Optional<BagDimensions> extractFromText(String text) {

        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        String normalized = text
                .replace("㎝", "cm")
                .replace("㎜", "mm");

        Matcher matcher =
                TRIPLE_DIMENSION_PATTERN.matcher(normalized);

        while (matcher.find()) {

            double first =
                    Double.parseDouble(matcher.group(1));

            double second =
                    Double.parseDouble(matcher.group(2));

            double third =
                    Double.parseDouble(matcher.group(3));

            String unit =
                    matcher.group(4).toLowerCase();

            if ("cm".equals(unit)) {
                first *= 10;
                second *= 10;
                third *= 10;
            }

            if (isReasonableBagSize(first, second, third)) {

                return Optional.of(
                        new BagDimensions(
                                first,
                                second,
                                third
                        )
                );
            }
        }

        return Optional.empty();
    }

    private boolean isReasonableBagSize(
            double widthMm,
            double heightMm,
            double depthMm
    ) {

        return widthMm >= 40
                && widthMm <= 1000

                && heightMm >= 40
                && heightMm <= 1000

                && depthMm >= 10
                && depthMm <= 1000;
    }

    public record BagDimensions(
            double widthMm,
            double heightMm,
            double depthMm
    ) {

        public double volume() {
            return widthMm * heightMm * depthMm;
        }
    }
}