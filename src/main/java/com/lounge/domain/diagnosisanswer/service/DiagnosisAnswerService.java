package com.lounge.domain.diagnosisanswer.service;

import com.lounge.domain.diagnosisanswer.repository.DiagnosisAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosisAnswerService {

    private final DiagnosisAnswerRepository diagnosisAnswerRepository;
}
