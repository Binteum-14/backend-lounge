package com.lounge.domain.diagnosis.entity;

import com.lounge.domain.diagnosisanswer.entity.DiagnosisAnswer;
import com.lounge.domain.recommendation.entity.Recommendation;
import com.lounge.domain.user.entity.User;
import com.lounge.global.entity.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnosis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diagnosis extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    @OneToMany(mappedBy = "diagnosis")
    private List<DiagnosisAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "diagnosis")
    private List<Recommendation> recommendations = new ArrayList<>();
}
