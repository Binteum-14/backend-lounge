package com.ddcj.binteum.domain.recommendationproduct.repository;

import com.ddcj.binteum.domain.recommendationproduct.entity.RecommendationProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationProductRepository extends JpaRepository<RecommendationProduct, Long> {
}
