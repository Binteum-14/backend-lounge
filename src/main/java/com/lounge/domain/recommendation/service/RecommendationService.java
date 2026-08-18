package com.lounge.domain.recommendation.service;

import com.lounge.domain.diagnosis.entity.Diagnosis;
import com.lounge.domain.diagnosis.exception.DiagnosisException;
import com.lounge.domain.diagnosis.exception.code.DiagnosisErrorCode;
import com.lounge.domain.diagnosis.repository.DiagnosisRepository;
import com.lounge.domain.diagnosisanswer.entity.DiagnosisAnswer;
import com.lounge.domain.diagnosisanswer.repository.DiagnosisAnswerRepository;
import com.lounge.domain.product.entity.Product;
import com.lounge.domain.product.repository.ProductRepository;
import com.lounge.domain.recommendation.client.OpenAiRecommendationClient;
import com.lounge.domain.recommendation.dto.AiRecommendationInput;
import com.lounge.domain.recommendation.dto.AiRecommendationResult;
import com.lounge.domain.recommendation.dto.request.RecommendationGenerateRequest;
import com.lounge.domain.recommendation.dto.request.RecommendationGenerateRequest.SelectedProductRequest;
import com.lounge.domain.recommendation.dto.response.RecommendationResponse;
import com.lounge.domain.recommendation.dto.response.RecommendationResponse.RecommendedProductResponse;
import com.lounge.domain.recommendation.entity.Recommendation;
import com.lounge.domain.recommendation.exception.RecommendationException;
import com.lounge.domain.recommendation.exception.code.RecommendationErrorCode;
import com.lounge.domain.recommendation.repository.RecommendationRepository;
import com.lounge.domain.recommendationproduct.entity.RecommendationProduct;
import com.lounge.domain.recommendationproduct.repository.RecommendationProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationProductRepository recommendationProductRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisAnswerRepository diagnosisAnswerRepository;
    private final ProductRepository productRepository;
    private final OpenAiRecommendationClient openAiRecommendationClient;

    @Transactional
    public RecommendationResponse generateRecommendation(
            Long userId,
            RecommendationGenerateRequest request
    ) {
        validateSelectedProducts(request.selectedProducts());

        Diagnosis diagnosis = diagnosisRepository
                .findByIdAndUser_Id(request.diagnosisId(), userId)
                .orElseThrow(() -> DiagnosisException.of(
                        DiagnosisErrorCode.DIAGNOSIS_NOT_FOUND
                ));

        List<DiagnosisAnswer> diagnosisAnswers =
                diagnosisAnswerRepository
                        .findAllByDiagnosis_IdOrderByQuestionNoAsc(
                                diagnosis.getId()
                        );

        if (diagnosisAnswers.isEmpty()) {
            throw RecommendationException.of(
                    RecommendationErrorCode.DIAGNOSIS_ANSWERS_NOT_FOUND
            );
        }

        List<SelectedProductRequest> selectedProducts =
                request.selectedProducts().stream()
                        .sorted(
                                Comparator.comparing(
                                        SelectedProductRequest::recommendationRank
                                )
                        )
                        .toList();

        List<Long> selectedProductIds = selectedProducts.stream()
                .map(SelectedProductRequest::productId)
                .toList();

        Map<Long, Product> productMap =
                productRepository.findAllById(selectedProductIds).stream()
                        .collect(
                                Collectors.toMap(
                                        Product::getId,
                                        Function.identity()
                                )
                        );

        if (productMap.size() != 3) {
            throw RecommendationException.of(
                    RecommendationErrorCode.SELECTED_PRODUCT_NOT_FOUND
            );
        }

        List<Product> orderedProducts = selectedProducts.stream()
                .map(selectedProduct -> {
                    Product product = productMap.get(
                            selectedProduct.productId()
                    );

                    if (product == null) {
                        throw RecommendationException.of(
                                RecommendationErrorCode
                                        .SELECTED_PRODUCT_NOT_FOUND
                        );
                    }

                    return product;
                })
                .toList();

        AiRecommendationInput aiInput = createAiInput(
                diagnosisAnswers,
                orderedProducts
        );

        AiRecommendationResult aiResult =
                openAiRecommendationClient.generate(aiInput);

        diagnosis.updateResultSummary(aiResult.resultSummary());

        Recommendation savedRecommendation =
                recommendationRepository.save(
                        Recommendation.create(
                                diagnosis.getUser(),
                                diagnosis
                        )
                );

        Map<Long, String> recommendationReasonMap =
                aiResult.products().stream()
                        .collect(
                                Collectors.toMap(
                                        AiRecommendationResult
                                                .ProductReason::productId,
                                        AiRecommendationResult
                                                .ProductReason
                                                ::recommendationReason
                                )
                        );

        List<RecommendationProduct> recommendationProducts =
                selectedProducts.stream()
                        .map(selectedProduct -> {
                            Product product = productMap.get(
                                    selectedProduct.productId()
                            );

                            String recommendationReason =
                                    recommendationReasonMap.get(
                                            selectedProduct.productId()
                                    );

                            return RecommendationProduct.create(
                                    savedRecommendation,
                                    product,
                                    selectedProduct.recommendationRank(),
                                    selectedProduct.matchScore(),
                                    recommendationReason
                            );
                        })
                        .toList();

        recommendationProductRepository.saveAll(
                recommendationProducts
        );

        List<RecommendedProductResponse> productResponses =
                selectedProducts.stream()
                        .map(selectedProduct -> {
                            Product product = productMap.get(
                                    selectedProduct.productId()
                            );

                            return new RecommendedProductResponse(
                                    product.getId(),
                                    product.getName(),
                                    product.getImageUrl(),
                                    product.getDetailUrl(),
                                    selectedProduct.recommendationRank(),
                                    selectedProduct.matchScore(),
                                    recommendationReasonMap.get(
                                            product.getId()
                                    )
                            );
                        })
                        .toList();

        return new RecommendationResponse(
                savedRecommendation.getId(),
                aiResult.resultSummary(),
                productResponses
        );
    }

    private AiRecommendationInput createAiInput(
            List<DiagnosisAnswer> diagnosisAnswers,
            List<Product> products
    ) {
        List<AiRecommendationInput.Answer> answers =
                diagnosisAnswers.stream()
                        .map(answer ->
                                new AiRecommendationInput.Answer(
                                        answer.getQuestionCode(),
                                        answer.getAnswerText()
                                )
                        )
                        .toList();

        List<AiRecommendationInput.CandidateProduct> candidateProducts =
                products.stream()
                        .map(product ->
                                new AiRecommendationInput.CandidateProduct(
                                        product.getId(),
                                        product.getName(),
                                        product.getCategory(),
                                        product.getPrice(),
                                        product.getDescription(),
                                        product.getStorageScore(),
                                        product.getVersatilityScore(),
                                        product.getTravelSuitabilityScore(),
                                        product.getCommuteSuitabilityScore(),
                                        product.getLaptopStorageAvailable(),
                                        product.getLaptopStorageScore(),
                                        product.getCabinSuitabilityScore()
                                )
                        )
                        .toList();

        return new AiRecommendationInput(
                answers,
                candidateProducts
        );
    }

    private void validateSelectedProducts(
            List<SelectedProductRequest> selectedProducts
    ) {
        if (selectedProducts == null
                || selectedProducts.size() != 3) {
            throw RecommendationException.of(
                    RecommendationErrorCode.INVALID_SELECTED_PRODUCTS
            );
        }

        long uniqueProductIdCount = selectedProducts.stream()
                .map(SelectedProductRequest::productId)
                .filter(productId -> productId != null)
                .distinct()
                .count();

        Set<Integer> recommendationRanks =
                selectedProducts.stream()
                        .map(
                                SelectedProductRequest
                                        ::recommendationRank
                        )
                        .collect(Collectors.toSet());

        if (uniqueProductIdCount != 3
                || !recommendationRanks.equals(
                        Set.of(1, 2, 3)
                )) {
            throw RecommendationException.of(
                    RecommendationErrorCode.INVALID_SELECTED_PRODUCTS
            );
        }
    }
}