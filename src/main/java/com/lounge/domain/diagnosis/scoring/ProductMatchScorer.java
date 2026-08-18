package com.lounge.domain.diagnosis.scoring;

import com.lounge.domain.diagnosis.exception.DiagnosisException;
import com.lounge.domain.diagnosis.exception.code.DiagnosisErrorCode;
import com.lounge.domain.product.entity.Product;

import java.util.Comparator;
import java.util.List;

public final class ProductMatchScorer {

    private ProductMatchScorer() {
    }

    public static List<ScoredProduct> pickTop3(
            List<Product> activeProducts,
            DiagnosisWeightTable.DerivedProfile profile
    ) {
        if (activeProducts.size() < 3) {
            throw DiagnosisException.of(DiagnosisErrorCode.INSUFFICIENT_PRODUCTS);
        }

        List<Product> candidates = activeProducts;
        if (profile.laptopRequired()) {
            List<Product> laptopCapable = activeProducts.stream()
                    .filter(product -> Boolean.TRUE.equals(product.getLaptopStorageAvailable()))
                    .toList();
            if (laptopCapable.size() >= 3) {
                candidates = laptopCapable;
            }
        }

        if (candidates.size() < 3) {
            throw DiagnosisException.of(DiagnosisErrorCode.INSUFFICIENT_PRODUCTS);
        }

        PriceBonusCalculator priceBonusCalculator =
                PriceBonusCalculator.of(candidates, profile.priceBonusMax());

        return candidates.stream()
                .map(product -> {
                    int base = baseScore(product, profile.weights());
                    int bonus = priceBonusCalculator.bonus(product);
                    int matchScore = Math.min(100, base + bonus);
                    return new ScoredProduct(product, matchScore);
                })
                .sorted(
                        Comparator.comparingInt(ScoredProduct::matchScore).reversed()
                                .thenComparing(scored -> scored.product().getId())
                )
                .limit(3)
                .toList();
    }

    static int baseScore(Product product, AxisWeights weights) {
        int weightSum = weights.sum();
        if (weightSum == 0) {
            return 0;
        }

        int raw =
                score(product.getStorageScore()) * weights.storage()
                        + score(product.getVersatilityScore()) * weights.versatility()
                        + score(product.getTravelSuitabilityScore()) * weights.travel()
                        + score(product.getCommuteSuitabilityScore()) * weights.commute()
                        + score(product.getLaptopStorageScore()) * weights.laptop()
                        + score(product.getCabinSuitabilityScore()) * weights.cabin();

        return (int) Math.round(raw / (5.0 * weightSum) * 100.0);
    }

    private static int score(Integer value) {
        return value == null ? 0 : value;
    }

    public record ScoredProduct(Product product, int matchScore) {
    }

    private record PriceBonusCalculator(long minPrice, long maxPrice, int maxBonus) {

        static PriceBonusCalculator of(List<Product> products, int maxBonus) {
            long minPrice = Long.MAX_VALUE;
            long maxPrice = Long.MIN_VALUE;
            for (Product product : products) {
                long price = effectivePrice(product);
                minPrice = Math.min(minPrice, price);
                maxPrice = Math.max(maxPrice, price);
            }
            return new PriceBonusCalculator(minPrice, maxPrice, maxBonus);
        }

        int bonus(Product product) {
            if (maxBonus == 0 || minPrice == maxPrice) {
                return 0;
            }
            long price = effectivePrice(product);
            return (int) Math.round(
                    maxBonus * (maxPrice - price) / (double) (maxPrice - minPrice)
            );
        }

        private static long effectivePrice(Product product) {
            return product.getPrice() == null ? Long.MAX_VALUE : product.getPrice();
        }
    }
}
