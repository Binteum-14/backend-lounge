package com.lounge.domain.diagnosisanswer.controller;

import com.lounge.domain.diagnosisanswer.service.DiagnosisAnswerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DiagnosisAnswer", description = "진단 답변 API")
@RestController
@RequestMapping("/api/diagnosisanswer")
@RequiredArgsConstructor
public class DiagnosisAnswerController {

    private final DiagnosisAnswerService diagnosisAnswerService;
}
