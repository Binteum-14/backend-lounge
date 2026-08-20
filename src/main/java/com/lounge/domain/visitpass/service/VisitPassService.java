package com.lounge.domain.visitpass.service;

import com.lounge.domain.diagnosis.DiagnosisQuestionCatalog;
import com.lounge.domain.diagnosis.entity.Diagnosis;
import com.lounge.domain.diagnosisanswer.entity.DiagnosisAnswer;
import com.lounge.domain.diagnosisanswer.repository.DiagnosisAnswerRepository;
import com.lounge.domain.recommendation.entity.Recommendation;
import com.lounge.domain.recommendationproduct.entity.RecommendationProduct;
import com.lounge.domain.recommendationproduct.exception.RecommendationProductException;
import com.lounge.domain.recommendationproduct.exception.code.RecommendationProductErrorCode;
import com.lounge.domain.recommendationproduct.repository.RecommendationProductRepository;
import com.lounge.domain.user.entity.User;
import com.lounge.domain.user.exception.UserException;
import com.lounge.domain.user.exception.code.UserErrorCode;
import com.lounge.domain.user.repository.UserRepository;
import com.lounge.domain.visitpass.dto.request.VisitPassIssueRequest;
import com.lounge.domain.visitpass.dto.response.VisitPassListResponse;
import com.lounge.domain.visitpass.dto.response.VisitPassPublicView;
import com.lounge.domain.visitpass.dto.response.VisitPassPublicView.AnswerView;
import com.lounge.domain.visitpass.dto.response.VisitPassPublicView.ProductView;
import com.lounge.domain.visitpass.dto.response.VisitPassResponse;
import com.lounge.domain.visitpass.entity.VisitPass;
import com.lounge.domain.visitpass.exception.VisitPassException;
import com.lounge.domain.visitpass.exception.code.VisitPassErrorCode;
import com.lounge.domain.visitpass.qr.QrCodeGenerator;
import com.lounge.domain.visitpass.repository.VisitPassRepository;
import com.lounge.global.config.properties.AppProperties;
import com.lounge.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.exception.SdkException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitPassService {

    private final VisitPassRepository visitPassRepository;
    private final UserRepository userRepository;
    private final RecommendationProductRepository recommendationProductRepository;
    private final DiagnosisAnswerRepository diagnosisAnswerRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final S3Service s3Service;
    private final AppProperties appProperties;

    @Transactional
    public VisitPassResponse issueVisitPass(Long userId, VisitPassIssueRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.of(UserErrorCode.USER_NOT_FOUND));

        RecommendationProduct recommendationProduct =
                recommendationProductRepository.findById(request.recommendationProductId())
                        .orElseThrow(() -> RecommendationProductException.of(
                                RecommendationProductErrorCode.RECOMMENDATION_PRODUCT_NOT_FOUND
                        ));

        Recommendation recommendation = recommendationProduct.getRecommendation();
        validateOwner(userId, recommendation);

        String publicToken = UUID.randomUUID().toString();
        String qrTargetUrl = appProperties.normalizedPublicUrl()
                + "/visit-passes/public/" + publicToken;
        String objectKey = "visit-pass/qr/" + publicToken + ".png";

        byte[] qrImageBytes;
        try {
            qrImageBytes = qrCodeGenerator.generatePng(qrTargetUrl);
        } catch (IllegalStateException e) {
            log.error("QR 이미지 생성 실패. qrTargetUrl={}", qrTargetUrl, e);
            throw VisitPassException.of(VisitPassErrorCode.VISIT_PASS_QR_GENERATE_FAILED);
        }

        String qrImageUrl;
        try {
            qrImageUrl = s3Service.uploadQrCode(qrImageBytes, objectKey);
        } catch (SdkException e) {
            log.error("QR 이미지 S3 업로드 실패. objectKey={}, qrTargetUrl={}",
                    objectKey, qrTargetUrl, e);
            throw VisitPassException.of(VisitPassErrorCode.VISIT_PASS_QR_UPLOAD_FAILED);
        }

        Diagnosis diagnosis = recommendation.getDiagnosis();
        LocalDate diagnosedAt = diagnosis.getCreatedAt().toLocalDate();

        VisitPass visitPass = visitPassRepository.save(
                VisitPass.create(
                        user,
                        recommendationProduct,
                        publicToken,
                        diagnosedAt,
                        qrImageUrl
                )
        );

        return VisitPassResponse.from(visitPass);
    }

    public VisitPassListResponse getVisitPasses(Long userId) {
        List<VisitPassResponse> visitPasses = visitPassRepository
                .findAllByUser_IdOrderByIdDesc(userId)
                .stream()
                .map(VisitPassResponse::from)
                .toList();

        return new VisitPassListResponse(visitPasses);
    }

    public VisitPassPublicView getPublicView(String publicToken) {
        VisitPass visitPass = visitPassRepository.findByPublicTokenWithRelations(publicToken)
                .orElseThrow(() -> VisitPassException.of(VisitPassErrorCode.VISIT_PASS_NOT_FOUND));

        Recommendation recommendation = visitPass.getRecommendationProduct().getRecommendation();
        Diagnosis diagnosis = recommendation.getDiagnosis();

        List<AnswerView> answers = diagnosisAnswerRepository
                .findAllByDiagnosis_IdOrderByQuestionNoAsc(diagnosis.getId())
                .stream()
                .map(this::toAnswerView)
                .toList();

        Long selectedRecommendationProductId = visitPass.getRecommendationProduct().getId();

        List<ProductView> products = recommendationProductRepository
                .findAllByRecommendation_IdOrderByRecommendationRankAsc(recommendation.getId())
                .stream()
                .map(recommendationProduct -> toProductView(
                        recommendationProduct,
                        selectedRecommendationProductId
                ))
                .toList();

        return new VisitPassPublicView(
                visitPass.getUser().getUsername(),
                visitPass.getIssuedDate(),
                diagnosis.getResultSummary(),
                answers,
                products
        );
    }

    private void validateOwner(Long userId, Recommendation recommendation) {
        User owner = recommendation.getUser();
        if (owner == null || !owner.getId().equals(userId)) {
            throw VisitPassException.of(VisitPassErrorCode.VISIT_PASS_FORBIDDEN);
        }
    }

    private AnswerView toAnswerView(DiagnosisAnswer answer) {
        String questionText = DiagnosisQuestionCatalog.questionText(
                answer.getQuestionNo(),
                answer.getQuestionCode()
        );
        String answerText = DiagnosisQuestionCatalog.answerText(
                answer.getQuestionNo(),
                answer.getAnswerCode(),
                answer.getAnswerText()
        );

        return new AnswerView(questionText, answerText);
    }

    private ProductView toProductView(
            RecommendationProduct recommendationProduct,
            Long selectedRecommendationProductId
    ) {
        return new ProductView(
                recommendationProduct.getRecommendationRank(),
                recommendationProduct.getProduct().getName(),
                recommendationProduct.getProduct().getImageUrl(),
                recommendationProduct.getRecommendationReason(),
                recommendationProduct.getId().equals(selectedRecommendationProductId)
        );
    }
}
