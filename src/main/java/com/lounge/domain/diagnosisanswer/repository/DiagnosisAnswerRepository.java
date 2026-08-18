package com.lounge.domain.diagnosisanswer.repository;

import com.lounge.domain.diagnosisanswer.entity.DiagnosisAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisAnswerRepository
        extends JpaRepository<DiagnosisAnswer, Long> {

    List<DiagnosisAnswer> findAllByDiagnosis_IdOrderByQuestionNoAsc(
            Long diagnosisId
    );
}