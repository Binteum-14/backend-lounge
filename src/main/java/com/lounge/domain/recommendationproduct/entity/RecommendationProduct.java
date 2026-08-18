package com.lounge.domain.recommendationproduct.entity;

import com.lounge.domain.product.entity.Product;
import com.lounge.domain.recommendation.entity.Recommendation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "recommendation_rank")
    private Integer recommendationRank;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    private RecommendationProduct(
            Recommendation recommendation,
            Product product,
            Integer recommendationRank,
            Integer matchScore,
            String recommendationReason
    ) {
        this.recommendation = recommendation;
        this.product = product;
        this.recommendationRank = recommendationRank;
        this.matchScore = matchScore;
        this.recommendationReason = recommendationReason;
    }

    public static RecommendationProduct create(
            Recommendation recommendation,
            Product product,
            Integer recommendationRank,
            Integer matchScore,
            String recommendationReason
    ) {
        return new RecommendationProduct(
                recommendation,
                product,
                recommendationRank,
                matchScore,
                recommendationReason
        );
    }
}