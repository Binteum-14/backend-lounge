package com.lounge.domain.ownerchat.service;

import com.lounge.domain.ownerchat.client.OpenAiOwnerCareClient;
import com.lounge.domain.ownerchat.dto.AiOwnerCareInput;
import com.lounge.domain.ownerchat.dto.AiOwnerCareResult;
import com.lounge.domain.ownerchat.dto.OwnerChatProductCandidate;
import com.lounge.domain.ownerchat.dto.OwnerChatProductMatchState;
import com.lounge.domain.ownerchat.dto.request.OwnerChatRequest;
import com.lounge.domain.ownerchat.dto.response.OwnerChatResponse;
import com.lounge.domain.ownerchat.exception.OwnerChatException;
import com.lounge.domain.ownerchat.exception.code.OwnerChatErrorCode;
import com.lounge.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerChatService {

    private static final String NEED_PRODUCT_NAME_MESSAGE =
            "정확한 관리 방법을 안내하려면 MCM 제품명이 필요해요. "
                    + "제품명을 포함해서 다시 질문해주세요. "
                    + "예: \"M Stark 비세토스 백팩이 비를 맞았는데 어떻게 관리해야 하나요?\"";

    private static final String PRODUCT_NOT_FOUND_MESSAGE =
            "입력하신 제품을 MCM 제품 데이터에서 찾지 못했어요. "
                    + "공식 제품명을 확인한 뒤 제품명을 포함해서 다시 질문해주세요.";

    private static final String AMBIGUOUS_PRODUCT_MESSAGE =
            "입력하신 내용과 관련된 MCM 제품이 여러 개 있어요. "
                    + "아래 후보 중 정확한 제품명을 포함해서 다시 질문해주세요.";

    private final OwnerChatProductResolver productResolver;

    private final OpenAiOwnerCareClient openAiOwnerCareClient;

    public OwnerChatResponse chat(
            OwnerChatRequest request
    ) {

        /*
         * history 형식부터 검증합니다.
         */
        validateHistory(
                request.history()
        );

        /*
         * 질문에서 제품 식별
         */
        OwnerChatProductResolver.Resolution resolution =
                productResolver.resolve(
                        request.message().trim(),
                        request.history()
                );

        /*
         * 제품명 없음
         *
         * 이 경우 OpenAI를 호출하지 않습니다.
         */
        if (resolution.state()
                == OwnerChatProductMatchState
                .NEED_PRODUCT_NAME) {

            return guideResponse(
                    OwnerChatProductMatchState
                            .NEED_PRODUCT_NAME,
                    NEED_PRODUCT_NAME_MESSAGE,
                    List.of()
            );
        }

        /*
         * 제품을 찾지 못함
         */
        if (resolution.state()
                == OwnerChatProductMatchState
                .PRODUCT_NOT_FOUND) {

            return guideResponse(
                    OwnerChatProductMatchState
                            .PRODUCT_NOT_FOUND,
                    PRODUCT_NOT_FOUND_MESSAGE,
                    List.of()
            );
        }

        /*
         * 여러 제품 검색됨
         */
        if (resolution.state()
                == OwnerChatProductMatchState
                .AMBIGUOUS_PRODUCT) {

            List<OwnerChatProductCandidate> candidates =
                    resolution.candidates()
                            .stream()
                            .map(
                                    OwnerChatProductCandidate::from
                            )
                            .toList();

            return guideResponse(
                    OwnerChatProductMatchState
                            .AMBIGUOUS_PRODUCT,
                    AMBIGUOUS_PRODUCT_MESSAGE,
                    candidates
            );
        }

        /*
         * 정확히 한 제품으로 확정
         */
        Product product =
                resolution.product();

        /*
         * 여기까지 왔을 때만 실제 OpenAI API를 호출합니다.
         */
        AiOwnerCareResult aiResult =
                openAiOwnerCareClient.generate(
                        createAiInput(
                                product,
                                request
                        )
                );

        return new OwnerChatResponse(
                OwnerChatProductMatchState.MATCHED,

                product.getId(),

                product.getName(),

                aiResult.answer(),

                List.of(),

                aiResult.suggestedQuestions()
        );
    }

    private OwnerChatResponse guideResponse(
            OwnerChatProductMatchState state,
            String answer,
            List<OwnerChatProductCandidate> candidates
    ) {

        return new OwnerChatResponse(
                state,
                null,
                null,
                answer,
                candidates,
                List.of()
        );
    }

    private AiOwnerCareInput createAiInput(
            Product product,
            OwnerChatRequest request
    ) {

        List<AiOwnerCareInput.Message> history =
                request.history()
                        .stream()
                        .map(message ->
                                new AiOwnerCareInput.Message(
                                        message.role()
                                                .name()
                                                .toLowerCase(),

                                        message.content()
                                                .trim()
                                )
                        )
                        .toList();

        AiOwnerCareInput.ProductContext productContext =
                new AiOwnerCareInput.ProductContext(

                        product.getId(),

                        valueOrNotProvided(
                                product.getSku()
                        ),

                        product.getName(),

                        product.getCategory(),

                        valueOrNotProvided(
                                product.getDescription()
                        ),

                        valueOrNotProvided(
                                product.getProductFeature()
                        ),

                        valueOrNotProvided(
                                product.getCareGuide()
                        )
                );

        return new AiOwnerCareInput(
                productContext,
                history,
                request.message().trim()
        );
    }

    /*
     * history는 반드시:
     *
     * USER
     * ASSISTANT
     * USER
     * ASSISTANT
     *
     * 완성된 쌍이어야 합니다.
     */
    private void validateHistory(
            List<OwnerChatRequest.HistoryMessage> history
    ) {

        if (history.isEmpty()) {
            return;
        }

        OwnerChatRequest.Role expectedRole =
                OwnerChatRequest.Role.USER;

        for (OwnerChatRequest.HistoryMessage message :
                history) {

            if (message.role()
                    != expectedRole) {

                throw OwnerChatException.of(
                        OwnerChatErrorCode
                                .INVALID_HISTORY
                );
            }

            expectedRole =
                    expectedRole
                            == OwnerChatRequest.Role.USER

                            ? OwnerChatRequest.Role.ASSISTANT

                            : OwnerChatRequest.Role.USER;
        }

        /*
         * 마지막 기록이 USER라면
         * 아직 USER/ASSISTANT 한 쌍이 완성되지 않은 것
         */
        if (history.get(
                        history.size() - 1
                )
                .role()
                != OwnerChatRequest.Role.ASSISTANT) {

            throw OwnerChatException.of(
                    OwnerChatErrorCode
                            .INVALID_HISTORY
            );
        }
    }

    private String valueOrNotProvided(
            String value
    ) {

        return value == null
                || value.isBlank()

                ? "제공되지 않음"

                : value.trim();
    }
}