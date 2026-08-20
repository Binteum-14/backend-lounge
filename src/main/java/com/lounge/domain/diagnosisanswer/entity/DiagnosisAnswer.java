package com.lounge.domain.diagnosisanswer.entity;

import com.lounge.domain.diagnosis.entity.Diagnosis;
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
@Table(name = "diagnosis_answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiagnosisAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(name = "question_no")
    private Integer questionNo;

    @Column(name = "question_code")
    private String questionCode;

    @Column(name = "answer_code")
    private String answerCode;

    @Column(name = "answer_text")
    private String answerText;

    private DiagnosisAnswer(
            Diagnosis diagnosis,
            Integer questionNo,
            String questionCode,
            String answerCode,
            String answerText
    ) {
        this.diagnosis = diagnosis;
        this.questionNo = questionNo;
        this.questionCode = questionCode;
        this.answerCode = answerCode;
        this.answerText = answerText;
    }

    public static DiagnosisAnswer create(
            Diagnosis diagnosis,
            Integer questionNo,
            String questionCode,
            String answerCode,
            String answerText
    ) {
        return new DiagnosisAnswer(
                diagnosis,
                questionNo,
                questionCode,
                answerCode,
                answerText
        );
    }
}
