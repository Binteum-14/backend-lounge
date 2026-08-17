package com.lounge.domain.recommendation.client;

import com.lounge.domain.recommendation.dto.AiRecommendationInput;
import com.lounge.domain.recommendation.dto.AiRecommendationResult;
import com.lounge.domain.recommendation.exception.RecommendationException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OpenAiRecommendationClientTest {

    private static final List<String> FORBIDDEN_EXPRESSIONS = List.of(
            "상세 스펙을 확인한 뒤 선택하세요",
            "무조건 구매해야 합니다",
            "누구에게나 잘 어울립니다",
            "최고의 제품입니다",
            "완벽한 제품입니다",
            "백엔드",
            "내부 점수",
            "JSON",
            "데이터 필드",
            "후보 선정 로직"
    );

    private static final Pattern EXPOSED_SCORE_PATTERN = Pattern.compile(
            "(?<!\\d)[1-5]\\s*/\\s*5(?!\\d)"
                    + "|[★☆]{2,}"
                    + "|(?<!\\d)\\d{1,3}\\s*점(?!\\w)"
    );

    @Autowired
    private OpenAiRecommendationClient client;

    @Test
    void rejectRequestWhenAnswersAreEmpty() {
        AiRecommendationInput validInput = createValidInput();

        AiRecommendationInput invalidInput = new AiRecommendationInput(
                List.of(),
                validInput.products()
        );

        assertThatThrownBy(() -> client.generate(invalidInput))
                .isInstanceOf(RecommendationException.class);
    }

    @Test
    void rejectRequestWhenProductCountIsNotThree() {
        AiRecommendationInput validInput = createValidInput();

        AiRecommendationInput invalidInput = new AiRecommendationInput(
                validInput.answers(),
                validInput.products().subList(0, 2)
        );

        assertThatThrownBy(() -> client.generate(invalidInput))
                .isInstanceOf(RecommendationException.class);
    }

    @Test
    void generateRecommendation() {
        boolean smokeTestEnabled = "true".equalsIgnoreCase(
                System.getenv("RUN_OPENAI_SMOKE_TEST")
        );

        Assumptions.assumeTrue(
                smokeTestEnabled,
                "RUN_OPENAI_SMOKE_TEST가 true가 아니므로 실제 OpenAI 호출을 건너뜁니다."
        );

        String apiKey = System.getenv("OPENAI_API_KEY");

        Assumptions.assumeTrue(
                apiKey != null && !apiKey.isBlank(),
                "OPENAI_API_KEY가 설정되지 않아 실제 OpenAI 호출을 건너뜁니다."
        );

        AiRecommendationInput input = createValidInput();

        AiRecommendationResult result = client.generate(input);

        validateSummary(result);
        validateProductOrder(result);
        validateProductReasons(result);
        validateUserVisibleContent(result);

        printResult(result);
    }

    private AiRecommendationInput createValidInput() {
        return new AiRecommendationInput(
                List.of(
                        new AiRecommendationInput.Answer(
                                "DAILY_ROUTINE",
                                "평일에는 노트북을 가지고 통학하고 주말에는 약속이 많습니다."
                        ),
                        new AiRecommendationInput.Answer(
                                "CARRIED_ITEMS",
                                "노트북, 충전기, 지갑, 휴대전화, 작은 파우치를 가지고 다닙니다."
                        ),
                        new AiRecommendationInput.Answer(
                                "PURCHASE_CONCERN",
                                "가격이 높은 만큼 실제로 자주 사용할 수 있을지 고민됩니다."
                        ),
                        new AiRecommendationInput.Answer(
                                "TRAVEL_FREQUENCY",
                                "학기 중에는 통학이 많고 방학에는 짧은 국내 여행을 자주 갑니다."
                        ),
                        new AiRecommendationInput.Answer(
                                "PREFERRED_USAGE",
                                "학교와 약속, 짧은 여행에서 두루 사용할 수 있는 제품을 원합니다."
                        )
                ),
                List.of(
                        new AiRecommendationInput.CandidateProduct(
                                101L,
                                "테스트 백팩",
                                "가방",
                                1_500_000L,
                                """
                                노트북과 일상 소지품을 구분해 넣을 수 있는 백팩 형태의 제품입니다.
                                양쪽 어깨로 착용할 수 있어 노트북과 충전기처럼 무게가 있는
                                소지품을 가지고 이동하는 상황에 적합합니다.
                                """,
                                5,
                                4,
                                4,
                                5,
                                "가능",
                                "적합"
                        ),
                        new AiRecommendationInput.CandidateProduct(
                                102L,
                                "테스트 토트백",
                                "가방",
                                1_300_000L,
                                """
                                넓은 입구와 손잡이를 갖춘 토트 형태의 제품입니다.
                                소지품을 자주 꺼내야 하는 상황에서 내부에 쉽게 접근할 수 있으며,
                                학교와 약속 등 서로 다른 일정에서 활용하기 좋은 형태입니다.
                                """,
                                4,
                                5,
                                3,
                                4,
                                "조건부 가능",
                                "적합"
                        ),
                        new AiRecommendationInput.CandidateProduct(
                                103L,
                                "테스트 크로스바디",
                                "가방",
                                900_000L,
                                """
                                휴대전화, 지갑, 작은 파우치 등 필수 소지품을 가볍게 넣고
                                이동할 수 있는 작은 크로스바디 형태의 제품입니다.
                                짐이 적은 약속이나 여행지에서 두 손을 자유롭게 사용할 수 있습니다.
                                """,
                                2,
                                4,
                                5,
                                3,
                                "불가능",
                                "적합"
                        )
                )
        );
    }

    private void validateSummary(AiRecommendationResult result) {
        assertThat(result)
                .as("OpenAI 추천 결과가 null이면 안 됩니다.")
                .isNotNull();

        assertThat(result.resultSummary())
                .as("전체 분석 결과가 비어 있으면 안 됩니다.")
                .isNotBlank();

        assertThat(result.resultSummary().length())
                .as("전체 분석 결과가 지나치게 짧으면 안 됩니다.")
                .isGreaterThanOrEqualTo(40);
    }

    private void validateProductOrder(AiRecommendationResult result) {
        assertThat(result.products())
                .as("추천 제품은 정확히 3개여야 합니다.")
                .hasSize(3);

        assertThat(result.products())
                .extracting(AiRecommendationResult.ProductReason::productId)
                .as("백엔드가 전달한 제품 순서와 productId를 그대로 유지해야 합니다.")
                .containsExactly(
                        101L,
                        102L,
                        103L
                );
    }

    private void validateProductReasons(AiRecommendationResult result) {
        assertThat(result.products())
                .allSatisfy(product -> {
                    assertThat(product.productId())
                            .as("productId가 비어 있으면 안 됩니다.")
                            .isNotNull();

                    assertThat(product.recommendationReason())
                            .as("제품별 추천 이유가 비어 있으면 안 됩니다.")
                            .isNotBlank();

                    assertThat(product.recommendationReason().length())
                            .as("제품별 추천 이유가 지나치게 짧으면 안 됩니다.")
                            .isGreaterThanOrEqualTo(40);
                });

        List<String> reasons = result.products().stream()
                .map(AiRecommendationResult.ProductReason::recommendationReason)
                .map(String::trim)
                .toList();

        assertThat(reasons)
                .as("서로 다른 제품에 완전히 같은 추천 설명을 사용하면 안 됩니다.")
                .doesNotHaveDuplicates();
    }

    private void validateUserVisibleContent(
            AiRecommendationResult result
    ) {
        List<String> reasons = result.products().stream()
                .map(AiRecommendationResult.ProductReason::recommendationReason)
                .toList();

        String combinedText =
                result.resultSummary() + " " + String.join(" ", reasons);

        for (String forbiddenExpression : FORBIDDEN_EXPRESSIONS) {
            assertThat(combinedText)
                    .as("사용자에게 금지 문구가 노출되면 안 됩니다: "
                            + forbiddenExpression)
                    .doesNotContain(forbiddenExpression);
        }

        boolean containsExposedScore =
                EXPOSED_SCORE_PATTERN.matcher(combinedText).find();

        assertThat(containsExposedScore)
                .as("3/5, 80점, 별점처럼 내부 점수가 노출되면 안 됩니다.")
                .isFalse();

        boolean containsPersonalizedContext = List.of(
                "노트북",
                "통학",
                "약속",
                "여행",
                "소지품",
                "이동"
        ).stream().anyMatch(combinedText::contains);

        assertThat(containsPersonalizedContext)
                .as("사용자의 생활 패턴이나 소지품이 분석 결과에 반영되어야 합니다.")
                .isTrue();
    }

    private void printResult(AiRecommendationResult result) {
        System.out.println();
        System.out.println("========== AI 전체 분석 ==========");
        System.out.println(result.resultSummary());

        System.out.println();
        System.out.println("========== 제품별 추천 이유 ==========");

        result.products().forEach(product -> {
            System.out.println();
            System.out.println("productId: " + product.productId());
            System.out.println(product.recommendationReason());
        });

        System.out.println();
        System.out.println("==================================");
    }
}