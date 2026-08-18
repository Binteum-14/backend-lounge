package com.lounge.domain.recommendationproduct.repository;

import com.lounge.domain.recommendationproduct.entity.RecommendationProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationProductRepository extends JpaRepository<RecommendationProduct, Long> {

    @EntityGraph(attributePaths = "product")
    List<RecommendationProduct> findAllByRecommendation_IdOrderByRecommendationRankAsc(
            Long recommendationId
    );
}
