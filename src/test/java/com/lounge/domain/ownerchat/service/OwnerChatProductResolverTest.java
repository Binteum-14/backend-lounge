package com.lounge.domain.ownerchat.service;

import com.lounge.domain.ownerchat.dto.OwnerChatProductMatchState;
import com.lounge.domain.ownerchat.dto.request.OwnerChatRequest;
import com.lounge.domain.product.entity.Product;
import com.lounge.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerChatProductResolverTest {

    @Mock
    private ProductRepository productRepository;

    private OwnerChatProductResolver resolver;

    @BeforeEach
    void setUp() {

        resolver =
                new OwnerChatProductResolver(
                        productRepository
                );
    }

    @Test
    void requestProductNameWhenQuestionHasNoProductName() {

        /*
         * 중요:
         * products()를 먼저 실행해서 mock Product들을 전부 만든 다음,
         * Repository stubbing을 해야 합니다.
         */
        List<Product> testProducts =
                products();

        when(
                productRepository
                        .findAllByActiveTrueOrderByNameAsc()
        )
                .thenReturn(
                        testProducts
                );

        OwnerChatProductResolver.Resolution result =
                resolver.resolve(
                        "비를 맞았는데 어떻게 관리해야 하나요?",
                        List.of()
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .NEED_PRODUCT_NAME
                );
    }

    @Test
    void returnSeveralCandidatesForBroadStarkQuestion() {

        List<Product> testProducts =
                products();

        when(
                productRepository
                        .findAllByActiveTrueOrderByNameAsc()
        )
                .thenReturn(
                        testProducts
                );

        OwnerChatProductResolver.Resolution result =
                resolver.resolve(
                        "스타크 백팩 관리법 알려줘",
                        List.of()
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .AMBIGUOUS_PRODUCT
                );

        assertThat(
                result.candidates()
        )
                .hasSize(3);
    }

    @Test
    void matchExactProductEvenWhenKoreanParticleFollowsName() {

        List<Product> testProducts =
                products();

        when(
                productRepository
                        .findAllByActiveTrueOrderByNameAsc()
        )
                .thenReturn(
                        testProducts
                );

        OwnerChatProductResolver.Resolution result =
                resolver.resolve(
                        "M Stark 비세토스 백팩이 비를 맞았는데 어떻게 관리해야 하나요?",
                        List.of()
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .MATCHED
                );

        assertThat(
                result.product()
                        .getId()
        )
                .isEqualTo(
                        102L
                );
    }

    @Test
    void matchKoreanStarkAliasWithoutSizePrefix() {

        List<Product> testProducts =
                products();

        when(
                productRepository
                        .findAllByActiveTrueOrderByNameAsc()
        )
                .thenReturn(
                        testProducts
                );

        OwnerChatProductResolver.Resolution result =
                resolver.resolve(
                        "스타크 비세토스 백팩 보관법 알려줘",
                        List.of()
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .MATCHED
                );

        assertThat(
                result.product()
                        .getId()
        )
                .isEqualTo(
                        102L
                );
    }

    @Test
    void reuseMostRecentMatchedProductFromUserHistoryForFollowUp() {

        List<Product> testProducts =
                products();

        when(
                productRepository
                        .findAllByActiveTrueOrderByNameAsc()
        )
                .thenReturn(
                        testProducts
                );

        List<OwnerChatRequest.HistoryMessage> history =
                List.of(
                        new OwnerChatRequest.HistoryMessage(
                                OwnerChatRequest.Role.USER,
                                "M Stark 비세토스 백팩이 비를 맞았어"
                        ),

                        new OwnerChatRequest.HistoryMessage(
                                OwnerChatRequest.Role.ASSISTANT,
                                "마른 천으로 물기를 눌러 제거해주세요."
                        )
                );

        OwnerChatProductResolver.Resolution result =
                resolver.resolve(
                        "그럼 보관은 어떻게 해야 해?",
                        history
                );

        assertThat(result.state())
                .isEqualTo(
                        OwnerChatProductMatchState
                                .MATCHED
                );

        assertThat(
                result.product()
                        .getId()
        )
                .isEqualTo(
                        102L
                );
    }

    private List<Product> products() {

        /*
         * Repository의 when(...) 실행 중에 이 메서드를 호출하지 않습니다.
         *
         * Product mock 생성과 Repository mock 설정을 분리해서
         * Mockito의 UnfinishedStubbingException을 방지합니다.
         */
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

        Product third =
                mockProduct(
                        103L,
                        "MMKGSVE05BK001",
                        "M Stark 갤러틱 갈라 양가죽 백팩"
                );

        Product fourth =
                mockProduct(
                        104L,
                        "MMKGATA03MT001",
                        "M Aren 노바 나일론 백팩"
                );

        return List.of(
                first,
                second,
                third,
                fourth
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
                .when(
                        product.getId()
                )
                .thenReturn(
                        id
                );

        lenient()
                .when(
                        product.getSku()
                )
                .thenReturn(
                        sku
                );

        lenient()
                .when(
                        product.getName()
                )
                .thenReturn(
                        name
                );

        return product;
    }
}