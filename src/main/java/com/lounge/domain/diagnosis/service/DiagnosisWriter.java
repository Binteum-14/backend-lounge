package com.lounge.domain.diagnosis.service;

import com.lounge.domain.diagnosis.DiagnosisQuestionCatalog.ResolvedAnswer;
import com.lounge.domain.diagnosis.entity.Diagnosis;
import com.lounge.domain.diagnosis.repository.DiagnosisRepository;
import com.lounge.domain.diagnosisanswer.entity.DiagnosisAnswer;
import com.lounge.domain.diagnosisanswer.repository.DiagnosisAnswerRepository;
import com.lounge.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosisWriter {

    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisAnswerRepository diagnosisAnswerRepository;

    @Transactional
    public Diagnosis save(User user, List<ResolvedAnswer> answers) {
        Diagnosis diagnosis = diagnosisRepository.save(Diagnosis.create(user));

        List<DiagnosisAnswer> diagnosisAnswers = answers.stream()
                .sorted(Comparator.comparingInt(ResolvedAnswer::questionNo))
                .map(answer -> DiagnosisAnswer.create(
                        diagnosis,
                        answer.questionNo(),
                        answer.questionCode(),
                        answer.answerCode(),
                        answer.answerText()
                ))
                .toList();

        diagnosisAnswerRepository.saveAll(diagnosisAnswers);
        return diagnosis;
    }
}
