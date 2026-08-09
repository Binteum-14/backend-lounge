package com.ddcj.binteum.domain.recommendation.entity;

import com.ddcj.binteum.domain.diagnosis.entity.Diagnosis;
import com.ddcj.binteum.domain.recommendationproduct.entity.RecommendationProduct;
import com.ddcj.binteum.domain.user.entity.User;
import com.ddcj.binteum.global.entity.CreatedAtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @OneToMany(mappedBy = "recommendation")
    private List<RecommendationProduct> recommendationProducts = new ArrayList<>();
}
