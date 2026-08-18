package com.lounge.domain.diagnosis.service;

import com.lounge.domain.diagnosis.DiagnosisQuestionCatalog;
import com.lounge.domain.diagnosis.DiagnosisQuestionCatalog.ResolvedAnswer;
import com.lounge.domain.diagnosis.dto.request.DiagnosisCompleteRequest;
import com.lounge.domain.diagnosis.dto.request.DiagnosisCompleteRequest.AnswerRequest;
import com.lounge.domain.diagnosis.entity.Diagnosis;
import com.lounge.domain.diagnosis.exception.DiagnosisException;
import com.lounge.domain.diagnosis.exception.code.DiagnosisErrorCode;
import com.lounge.domain.diagnosis.scoring.DiagnosisWeightTable;
import com.lounge.domain.diagnosis.scoring.ProductMatchScorer;
import com.lounge.domain.diagnosis.scoring.ProductMatchScorer.ScoredProduct;
import com.lounge.domain.product.entity.Product;
import com.lounge.domain.product.repository.ProductRepository;
import com.lounge.domain.recommendation.dto.request.RecommendationGenerateRequest;
import com.lounge.domain.recommendation.dto.request.RecommendationGenerateRequest.SelectedProductRequest;
import com.lounge.domain.recommendation.dto.response.RecommendationResponse;
import com.lounge.domain.recommendation.service.RecommendationService;
import com.lounge.domain.user.entity.User;
import com.lounge.domain.user.exception.UserException;
import com.lounge.domain.user.exception.code.UserErrorCode;
import com.lounge.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisWriter diagnosisWriter;
    private final ProductRepository productRepository;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommendationResponse complete(
            Long userId,
            DiagnosisCompleteRequest request
    ) {
        List<ResolvedAnswer> answers = resolveAnswers(request.answers());
        DiagnosisWeightTable.DerivedProfile profile = DiagnosisWeightTable.derive(answers);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.of(UserErrorCode.USER_NOT_FOUND));

        Diagnosis diagnosis = diagnosisWriter.save(user, answers);

        List<Product> activeProducts = productRepository.findAllByActiveTrue();
        List<ScoredProduct> top3 = ProductMatchScorer.pickTop3(activeProducts, profile);

        List<SelectedProductRequest> selectedProducts = new ArrayList<>();
        for (int rank = 0; rank < top3.size(); rank++) {
            ScoredProduct scored = top3.get(rank);
            selectedProducts.add(new SelectedProductRequest(
                    scored.product().getId(),
                    rank + 1,
                    scored.matchScore()
            ));
        }

        return recommendationService.generateRecommendation(
                userId,
                new RecommendationGenerateRequest(diagnosis.getId(),selectedProducts)
        );
    }

    private List<ResolvedAnswer> resolveAnswers(List<AnswerRequest> answers) {
        Set<Integer> questionNos = answers.stream()
                .map(AnswerRequest::questionNo)
                .collect(Collectors.toSet());

        if (!questionNos.equals(DiagnosisQuestionCatalog.REQUIRED_QUESTION_NOS)) {
            throw DiagnosisException.of(DiagnosisErrorCode.INVALID_ANSWERS);
        }

        return answers.stream()
                .map(answer -> DiagnosisQuestionCatalog.resolve(
                        answer.questionNo(),
                        answer.answerNo()
                ))
                .toList();
    }
}
