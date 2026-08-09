package com.ddcj.binteum.domain.product.entity;

import com.ddcj.binteum.domain.recommendationproduct.entity.RecommendationProduct;
import com.ddcj.binteum.domain.visitpassproduct.entity.VisitPassProduct;
import com.ddcj.binteum.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String name;

    private String category;

    private Long price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "detail_url")
    private String detailUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "storage_score")
    private Integer storageScore;

    @Column(name = "versatility_score")
    private Integer versatilityScore;

    @Column(name = "travel_suitability_score")
    private Integer travelSuitabilityScore;

    @Column(name = "commute_suitability_score")
    private Integer commuteSuitabilityScore;

    @Column(name = "laptop_storage_grade")
    private String laptopStorageGrade;

    @Column(name = "cabin_suitability_grade")
    private String cabinSuitabilityGrade;

    @Column(name = "waterproof_grade")
    private String waterproofGrade;

    private Boolean active;

    @OneToMany(mappedBy = "product")
    private List<RecommendationProduct> recommendationProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<VisitPassProduct> visitPassProducts = new ArrayList<>();
}
