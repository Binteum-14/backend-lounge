package com.lounge.domain.ownerchat.service;

import com.lounge.domain.ownerchat.client.OpenAiOwnerCareClient;
import com.lounge.domain.ownerchat.dto.AiOwnerCareInput;
import com.lounge.domain.ownerchat.dto.AiOwnerCareResult;
import com.lounge.domain.ownerchat.dto.OwnerChatProductMatchState;
import com.lounge.domain.ownerchat.dto.request.OwnerChatRequest;
import com.lounge.domain.ownerchat.dto.response.OwnerChatResponse;
import com.lounge.domain.ownerchat.exception.OwnerChatException;
import com.lounge.domain.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerChatServiceTest {

    @Mock
    private OwnerChatProductResolver productResolver;

    @Mock
    private OpenAiOwnerCareClient openAiOwnerCareClient;

    @InjectMocks
    private OwnerChatService ownerChatService;

    @Test
    void requestProductNameWhenProductCannotBeIdentified() {

        OwnerChatRequest request =
                new OwnerChatRequest(
                        "비를 맞았는데 어떻게 관리해야 하나요?",
                        List.of()
                );

        when(
                productResolver.resolve(
                        request.message(),
                        request.history()
                )
        )
                .thenReturn(
                        OwnerChatProductResolver
                                .Resolution
                                .needProductName()
                );

        OwnerChatResponse result =
                ownerChatService.chat(
                        request
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .NEED_PRODUCT_NAME
                );

        assertThat(result.answer())
                .contains("제품명");

        assertThat(result.candidates())
                .isEmpty();

        verify(
                openAiOwnerCareClient,
                never()
        ).generate(any());
    }

    @Test
    void returnCandidatesWhenSeveralProductsMatch() {

        Product first =
                mockProduct(
                        101L,
                        "MMKGSVE034B001",
                        "S Stark 사이드 스터드 비세토스 백팩"
                );

        Product second =
                mockProduct(
                        102L,
                        "MMKFAVE09K8001",
                        "M Stark 비세토스 백팩"
                );

        OwnerChatRequest request =
                new OwnerChatRequest(
                        "스타크 백팩 관리법 알려줘",
                        List.of()
                );

        when(
                productResolver.resolve(
                        request.message(),
                        request.history()
                )
        )
                .thenReturn(
                        OwnerChatProductResolver
                                .Resolution
                                .ambiguous(
                                        List.of(
                                                first,
                                                second
                                        )
                                )
                );

        OwnerChatResponse result =
                ownerChatService.chat(
                        request
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .AMBIGUOUS_PRODUCT
                );

        assertThat(result.candidates())
                .hasSize(2);

        assertThat(result.candidates())
                .extracting(
                        candidate ->
                                candidate.name()
                )
                .containsExactly(
                        "S Stark 사이드 스터드 비세토스 백팩",
                        "M Stark 비세토스 백팩"
                );

        verify(
                openAiOwnerCareClient,
                never()
        ).generate(any());
    }

    @Test
    void callOpenAiOnlyWhenOneProductIsMatched() {

        Product product =
                mockProduct(
                        101L,
                        "MMKFAVE09K8001",
                        "M Stark 비세토스 백팩"
                );

        OwnerChatRequest request =
                new OwnerChatRequest(
                        "M Stark 비세토스 백팩이 비를 맞았는데 어떻게 관리해야 하나요?",

                        List.of(
                                new OwnerChatRequest.HistoryMessage(
                                        OwnerChatRequest.Role.USER,
                                        "평소에는 어떻게 보관해야 하나요? M Stark 비세토스 백팩입니다."
                                ),

                                new OwnerChatRequest.HistoryMessage(
                                        OwnerChatRequest.Role.ASSISTANT,
                                        "제품명을 확인했습니다."
                                )
                        )
                );

        when(
                productResolver.resolve(
                        request.message(),
                        request.history()
                )
        )
                .thenReturn(
                        OwnerChatProductResolver
                                .Resolution
                                .matched(product)
                );

        when(
                openAiOwnerCareClient.generate(
                        any(
                                AiOwnerCareInput.class
                        )
                )
        )
                .thenReturn(
                        new AiOwnerCareResult(
                                "마른 부드러운 천으로 물기를 눌러 제거한 뒤 그늘에서 자연 건조하세요.",

                                List.of(
                                        "형태 잡는 방법",
                                        "평소 보관 방법"
                                )
                        )
                );

        OwnerChatResponse result =
                ownerChatService.chat(
                        request
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .MATCHED
                );

        assertThat(result.productId())
                .isEqualTo(101L);

        assertThat(result.productName())
                .isEqualTo(
                        "M Stark 비세토스 백팩"
                );

        assertThat(result.answer())
                .contains(
                        "자연 건조"
                );

        assertThat(result.candidates())
                .isEmpty();

        ArgumentCaptor<AiOwnerCareInput> inputCaptor =
                ArgumentCaptor.forClass(
                        AiOwnerCareInput.class
                );

        verify(
                openAiOwnerCareClient
        )
                .generate(
                        inputCaptor.capture()
                );

        AiOwnerCareInput input =
                inputCaptor.getValue();

        assertThat(
                input.product()
                        .productId()
        )
                .isEqualTo(101L);

        assertThat(
                input.product()
                        .name()
        )
                .isEqualTo(
                        "M Stark 비세토스 백팩"
                );

        assertThat(
                input.product()
                        .careGuide()
        )
                .contains(
                        "부드러운 마른 천"
                );

        assertThat(
                input.history()
        )
                .extracting(
                        AiOwnerCareInput.Message::role
                )
                .containsExactly(
                        "user",
                        "assistant"
                );
    }

    @Test
    void rejectHistoryThatDoesNotContainCompletePairs() {

        OwnerChatRequest request =
                new OwnerChatRequest(
                        "이어서 알려주세요.",

                        List.of(
                                new OwnerChatRequest.HistoryMessage(
                                        OwnerChatRequest.Role.USER,
                                        "M Stark 비세토스 백팩 관리법 알려줘"
                                )
                        )
                );

        assertThatThrownBy(
                () ->
                        ownerChatService.chat(
                                request
                        )
        )
                .isInstanceOf(
                        OwnerChatException.class
                );

        verify(
                productResolver,
                never()
        )
                .resolve(
                        any(),
                        any()
                );

        verify(
                openAiOwnerCareClient,
                never()
        )
                .generate(
                        any()
                );
    }

    private Product mockProduct(
            Long id,
            String sku,
            String name
    ) {

        Product product =
                org.mockito.Mockito.mock(
                        Product.class
                );

        lenient()
                .when(product.getId())
                .thenReturn(id);

        lenient()
                .when(product.getSku())
                .thenReturn(sku);

        lenient()
                .when(product.getName())
                .thenReturn(name);

        lenient()
                .when(product.getCategory())
                .thenReturn("가방");

        lenient()
                .when(product.getDescription())
                .thenReturn(
                        "비세토스 패턴의 백팩입니다."
                );

        lenient()
                .when(product.getProductFeature())
                .thenReturn(
                        "지퍼 여밈과 상단 핸들이 있습니다."
                );

        lenient()
                .when(product.getCareGuide())
                .thenReturn(
                        "부드러운 마른 천으로 가볍게 닦아주세요."
                );

        return product;
    }
}