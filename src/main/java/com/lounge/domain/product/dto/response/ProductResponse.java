package com.lounge.domain.product.dto.response;

import com.lounge.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String category;
    private Long price;
    private String imageUrl;
    private String detailUrl;
    private String description;
    private String productFeature;
    private String careGuide;
    private Integer storageScore;
    private Integer versatilityScore;
    private Integer travelSuitabilityScore;
    private Integer commuteSuitabilityScore;
    private Boolean laptopStorageAvailable;
    private Integer laptopStorageScore;
    private Integer cabinSuitabilityScore;

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getImageUrl(),
                product.getDetailUrl(),
                product.getDescription(),
                product.getProductFeature(),
                product.getCareGuide(),
                product.getStorageScore(),
                product.getVersatilityScore(),
                product.getTravelSuitabilityScore(),
                product.getCommuteSuitabilityScore(),
                product.getLaptopStorageAvailable(),
                product.getLaptopStorageScore(),
                product.getCabinSuitabilityScore()
        );
    }
}
