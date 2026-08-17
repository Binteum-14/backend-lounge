package com.lounge.domain.product.entity;

import com.lounge.domain.recommendationproduct.entity.RecommendationProduct;
import com.lounge.domain.visitpassproduct.entity.VisitPassProduct;
import com.lounge.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    private String name;

    private String category;

    private Long price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "detail_url", columnDefinition = "TEXT")
    private String detailUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "product_feature", columnDefinition = "TEXT")
    private String productFeature;

    @Column(name = "care_guide", columnDefinition = "TEXT")
    private String careGuide;

    @Column(name = "storage_score")
    private Integer storageScore;

    @Column(name = "versatility_score")
    private Integer versatilityScore;

    @Column(name = "travel_suitability_score")
    private Integer travelSuitabilityScore;

    @Column(name = "commute_suitability_score")
    private Integer commuteSuitabilityScore;

    @Column(name = "laptop_storage_available")
    private Boolean laptopStorageAvailable;

    @Column(name = "laptop_storage_score")
    private Integer laptopStorageScore;

    @Column(name = "cabin_suitability_score")
    private Integer cabinSuitabilityScore;

    private Boolean active;

    @Column(name = "source_collected_at")
    private LocalDate sourceCollectedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<RecommendationProduct> recommendationProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<VisitPassProduct> visitPassProducts = new ArrayList<>();
}
