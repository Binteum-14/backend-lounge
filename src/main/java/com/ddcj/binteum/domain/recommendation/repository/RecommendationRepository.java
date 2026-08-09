package com.ddcj.binteum.domain.recommendation.repository;

import com.ddcj.binteum.domain.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
