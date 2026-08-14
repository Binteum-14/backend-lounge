package com.lounge.domain.recommendationproduct.repository;

import com.lounge.domain.recommendationproduct.entity.RecommendationProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationProductRepository extends JpaRepository<RecommendationProduct, Long> {
}
