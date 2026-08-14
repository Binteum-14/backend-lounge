package com.lounge.domain.recommendation.repository;

import com.lounge.domain.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Modifying(clearAutomatically = true)
    @Query("update Recommendation r set r.user = null where r.user.id = :userId")
    int detachUser(@Param("userId") Long userId);
}
