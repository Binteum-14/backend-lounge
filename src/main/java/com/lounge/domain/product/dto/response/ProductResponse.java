package com.lounge.domain.product.dto.response;

import com.lounge.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String category;
    private Long price;
    private String imageUrl;
    private String detailUrl;
    private String description;
    private Integer storageScore;
    private Integer versatilityScore;
    private Integer travelSuitabilityScore;
    private Integer commuteSuitabilityScore;
    private String laptopStorageGrade;
    private String cabinSuitabilityGrade;
    private String waterproofGrade;

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getImageUrl(),
                product.getDetailUrl(),
                product.getDescription(),
                product.getStorageScore(),
                product.getVersatilityScore(),
                product.getTravelSuitabilityScore(),
                product.getCommuteSuitabilityScore(),
                product.getLaptopStorageGrade(),
                product.getCabinSuitabilityGrade(),
                product.getWaterproofGrade()
        );
    }
}
