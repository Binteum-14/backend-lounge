package com.lounge.domain.visitpass.repository;

import com.lounge.domain.visitpass.entity.VisitPass;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitPassRepository extends JpaRepository<VisitPass, Long> {

    @EntityGraph(attributePaths = "user")
    List<VisitPass> findAllByUser_IdOrderByIdDesc(Long userId);

    @Query("""
            select vp from VisitPass vp
            join fetch vp.user
            join fetch vp.recommendationProduct rp
            join fetch rp.recommendation r
            join fetch r.diagnosis
            where vp.publicToken = :publicToken
            """)
    Optional<VisitPass> findByPublicTokenWithRelations(
            @Param("publicToken") String publicToken
    );

    void deleteByUser_Id(Long userId);
}
