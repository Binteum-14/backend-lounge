package com.lounge.domain.ownerchat.service;

import com.lounge.domain.ownerchat.dto.OwnerChatProductMatchState;
import com.lounge.domain.ownerchat.dto.request.OwnerChatRequest;
import com.lounge.domain.product.entity.Product;
import com.lounge.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OwnerChatProductResolver {

    private static final int MAX_CANDIDATES = 5;

    /*
     * 데이터셋에 Stark/Aren/Toni처럼 영문 표기가 섞여 있기 때문에
     * 사용자가 한글로 입력해도 같은 제품군으로 검색할 수 있도록 맞춰줍니다.
     */
    private static final Map<String, String> PRODUCT_NAME_ALIASES =
            Map.ofEntries(
                    Map.entry("스타크", "stark"),
                    Map.entry("아렌", "aren"),
                    Map.entry("토니", "toni"),
                    Map.entry("리즈", "liz"),
                    Map.entry("트레이시", "tracy"),
                    Map.entry("피나", "pina"),
                    Map.entry("오토머", "ottomar"),
                    Map.entry("오토마르", "ottomar"),
                    Map.entry("데사우", "dessau"),
                    Map.entry("다이아몬드", "diamond"),
                    Map.entry("디아망", "diamant"),
                    Map.entry("엘라", "ella"),
                    Map.entry("밀라", "milla"),
                    Map.entry("히멜", "himmel"),
                    Map.entry("트라비아", "travia"),
                    Map.entry("푸르스텐", "fursten"),
                    Map.entry("퓌르스텐", "fursten"),
                    Map.entry("클래식", "klassik")
            );

    /*
     * "백팩", "가방"처럼 너무 일반적인 단어만으로
     * 특정 제품 하나를 선택하지 않도록 제외합니다.
     */
    private static final Set<String> GENERIC_PRODUCT_TOKENS =
            Set.of(
                    "mcm",
                    "x",
                    "with",
                    "the",
                    "in",

                    "s",
                    "m",
                    "l",
                    "xl",
                    "xs",
                    "xxs",
                    "xxl",

                    "가방",
                    "백",
                    "백팩",
                    "숄더백",
                    "크로스백",
                    "크로스바디",
                    "토트",
                    "토트백",
                    "쇼퍼",
                    "호보",
                    "파우치",
                    "케이스",

                    "지갑",
                    "카드지갑",
                    "카드",
                    "홀더",

                    "위켄더",

                    "향수",
                    "퍼퓸",
                    "오드퍼퓸",
                    "오드뚜왈렛"
            );

    private final ProductRepository productRepository;

    public Resolution resolve(
            String message,
            List<OwnerChatRequest.HistoryMessage> history
    ) {

        List<Product> products =
                productRepository.findAllByActiveTrueOrderByNameAsc();

        /*
         * DB에 활성 제품 자체가 없는 비정상 상황
         */
        if (products.isEmpty()) {
            return Resolution.productNotFound();
        }

        /*
         * 1순위:
         * 현재 사용자가 새로 입력한 message에서 제품을 찾습니다.
         */
        Resolution currentResult =
                resolveFromText(message, products);

        if (currentResult.state()
                != OwnerChatProductMatchState.NEED_PRODUCT_NAME) {

            return currentResult;
        }

        /*
         * 현재 질문에 제품명이 없으면
         * 이전 USER 대화에서 가장 최근 제품을 찾습니다.
         *
         * 예:
         * USER: M Stark 비세토스 백팩이 비 맞았어
         * AI: ...
         * USER: 그럼 보관은 어떻게 해?
         *
         * 두 번째 질문에서도 Stark 제품을 계속 사용합니다.
         */
        for (int i = history.size() - 1; i >= 0; i--) {

            OwnerChatRequest.HistoryMessage historyMessage =
                    history.get(i);

            /*
             * AI의 답변에 후보 제품명이 들어 있을 수 있으므로
             * ASSISTANT 메시지에서는 제품을 찾지 않습니다.
             */
            if (historyMessage.role()
                    != OwnerChatRequest.Role.USER) {

                continue;
            }

            Resolution historyResult =
                    resolveFromText(
                            historyMessage.content(),
                            products
                    );

            if (historyResult.state()
                    == OwnerChatProductMatchState.MATCHED) {

                return historyResult;
            }

            /*
             * 이전 질문 자체가 아직 애매했던 경우에는
             * 더 오래된 제품을 잘못 가져오지 않습니다.
             */
            if (historyResult.state()
                    == OwnerChatProductMatchState.AMBIGUOUS_PRODUCT
                    || historyResult.state()
                    == OwnerChatProductMatchState.PRODUCT_NOT_FOUND) {

                break;
            }
        }

        return Resolution.needProductName();
    }

    private Resolution resolveFromText(
            String text,
            List<Product> products
    ) {

        String normalizedText = normalize(text);

        if (normalizedText.isBlank()) {
            return Resolution.needProductName();
        }

        /*
         * 1. SKU가 질문에 직접 들어간 경우
         */
        List<Product> skuMatches =
                products.stream()
                        .filter(product ->
                                containsSku(
                                        normalizedText,
                                        product.getSku()
                                )
                        )
                        .toList();

        if (!skuMatches.isEmpty()) {
            return singleOrAmbiguous(skuMatches);
        }

        /*
         * 2. 전체 공식 제품명이 들어간 경우
         *
         * 예:
         * "M Stark 비세토스 백팩이 비를 맞았어요"
         */
        List<Product> fullNameMatches =
                longestContainedNameMatches(
                        normalizedText,
                        products,
                        false
                );

        if (!fullNameMatches.isEmpty()) {
            return singleOrAmbiguous(fullNameMatches);
        }

        /*
         * 3. 맨 앞 S/M/L 사이즈를 제외한 제품명 검색
         *
         * DB:
         * "M Stark 비세토스 백팩"
         *
         * 사용자:
         * "스타크 비세토스 백팩 관리법 알려줘"
         *
         * 이런 경우도 매칭됩니다.
         */
        List<Product> nameWithoutSizeMatches =
                longestContainedNameMatches(
                        normalizedText,
                        products,
                        true
                );

        if (!nameWithoutSizeMatches.isEmpty()) {
            return singleOrAmbiguous(
                    nameWithoutSizeMatches
            );
        }

        /*
         * 4. 전체 제품명까지는 입력하지 않은 경우
         *
         * 예:
         * "스타크 백팩"
         *
         * 제품 이름에 실제 존재하는 핵심 단어를 뽑아서
         * 후보 제품을 찾습니다.
         */
        Set<String> productVocabulary =
                createProductVocabulary(products);

        Set<String> queryTokens =
                new LinkedHashSet<>();

        for (String token : tokenize(normalizedText)) {

            if (productVocabulary.contains(token)
                    && !GENERIC_PRODUCT_TOKENS.contains(token)) {

                queryTokens.add(token);
            }
        }

        /*
         * 제품을 특정할 만한 단어가 전혀 없음
         *
         * 예:
         * "비 맞았는데 어떻게 관리해?"
         */
        if (queryTokens.isEmpty()) {
            return Resolution.needProductName();
        }

        /*
         * 질문에 포함된 핵심 제품 단어를 모두 갖는 제품 검색
         */
        List<Product> partialMatches =
                products.stream()
                        .filter(product -> {

                            Set<String> productTokens =
                                    new LinkedHashSet<>(
                                            tokenize(
                                                    normalizeSearchName(
                                                            product.getName()
                                                    )
                                            )
                                    );

                            return productTokens
                                    .containsAll(queryTokens);
                        })
                        .sorted(
                                Comparator.comparing(
                                        product ->
                                                safe(product.getName()),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        if (partialMatches.isEmpty()) {
            return Resolution.productNotFound();
        }

        return singleOrAmbiguous(partialMatches);
    }

    private List<Product> longestContainedNameMatches(
            String normalizedText,
            List<Product> products,
            boolean removeLeadingSize
    ) {

        List<NameMatch> matches =
                new ArrayList<>();

        for (Product product : products) {

            String normalizedName =
                    removeLeadingSize
                            ? normalizeSearchName(
                                    product.getName()
                            )
                            : normalize(
                                    product.getName()
                            );

            if (normalizedName.length() < 3) {
                continue;
            }

            /*
             * 뒤에 "이", "가" 같은 한국어 조사가 붙어도
             * 인식되도록 contains를 사용합니다.
             */
            if (normalizedText.contains(
                    normalizedName
            )) {

                matches.add(
                        new NameMatch(
                                product,
                                normalizedName.length()
                        )
                );
            }
        }

        /*
         * 여러 이름이 동시에 포함되어 있다면
         * 가장 구체적인(가장 긴) 제품명을 우선합니다.
         */
        int maxLength =
                matches.stream()
                        .mapToInt(NameMatch::length)
                        .max()
                        .orElse(0);

        return matches.stream()
                .filter(match ->
                        match.length() == maxLength
                )
                .map(NameMatch::product)
                .toList();
    }

    private Resolution singleOrAmbiguous(
            List<Product> matches
    ) {

        /*
         * 혹시 동일 productId가 중복되어 들어오더라도
         * 한 번만 남깁니다.
         */
        List<Product> distinctMatches =
                matches.stream()
                        .collect(
                                java.util.stream.Collectors
                                        .toMap(
                                                Product::getId,
                                                product -> product,
                                                (left, right) ->
                                                        left,
                                                java.util.LinkedHashMap::new
                                        )
                        )
                        .values()
                        .stream()
                        .toList();

        /*
         * 정확히 하나
         */
        if (distinctMatches.size() == 1) {

            return Resolution.matched(
                    distinctMatches.get(0)
            );
        }

        /*
         * 여러 개라면 절대로 하나를 마음대로 선택하지 않고
         * 최대 5개까지만 후보로 반환합니다.
         */
        return Resolution.ambiguous(
                distinctMatches.stream()
                        .sorted(
                                Comparator.comparing(
                                        product ->
                                                safe(
                                                        product.getName()
                                                ),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .limit(MAX_CANDIDATES)
                        .toList()
        );
    }

    private Set<String> createProductVocabulary(
            List<Product> products
    ) {

        Set<String> vocabulary =
                new LinkedHashSet<>();

        for (Product product : products) {

            for (String token :
                    tokenize(
                            normalizeSearchName(
                                    product.getName()
                            )
                    )) {

                if (token.length() >= 2
                        && !GENERIC_PRODUCT_TOKENS
                        .contains(token)) {

                    vocabulary.add(token);
                }
            }
        }

        return vocabulary;
    }

    private boolean containsSku(
            String normalizedText,
            String sku
    ) {

        if (sku == null || sku.isBlank()) {
            return false;
        }

        String normalizedSku =
                normalize(sku);

        return normalizedText.contains(
                normalizedSku
        );
    }

    /*
     * 제품명 맨 앞의 S, M, L 등을 제거합니다.
     */
    private String normalizeSearchName(
            String value
    ) {

        String normalized =
                normalize(value);

        return normalized.replaceFirst(
                "^(?:s m|xxs|xxl|xs|xl|s|m|l)\\s+",
                ""
        );
    }

    private List<String> tokenize(
            String normalizedText
    ) {

        if (normalizedText == null
                || normalizedText.isBlank()) {

            return List.of();
        }

        return List.of(
                normalizedText.split("\\s+")
        );
    }

    /*
     * 한글/영문/기호 차이를 최대한 동일하게 맞춥니다.
     */
    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        String normalized =
                Normalizer.normalize(
                                value,
                                Normalizer.Form.NFKC
                        )
                        .toLowerCase(Locale.ROOT)
                        .replace('–', '-')
                        .replace('—', '-');

        /*
         * 스타크 ↔ Stark 등 표기 통일
         */
        for (Map.Entry<String, String> alias :
                PRODUCT_NAME_ALIASES.entrySet()) {

            normalized =
                    normalized.replace(
                            alias.getKey(),
                            alias.getValue()
                    );
        }

        return normalized
                .replaceAll(
                        "[^\\p{L}\\p{N}]+",
                        " "
                )
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value;
    }

    private record NameMatch(
            Product product,
            int length
    ) {
    }

    public record Resolution(

            OwnerChatProductMatchState state,

            Product product,

            List<Product> candidates

    ) {

        public static Resolution needProductName() {

            return new Resolution(
                    OwnerChatProductMatchState
                            .NEED_PRODUCT_NAME,
                    null,
                    List.of()
            );
        }

        public static Resolution productNotFound() {

            return new Resolution(
                    OwnerChatProductMatchState
                            .PRODUCT_NOT_FOUND,
                    null,
                    List.of()
            );
        }

        public static Resolution ambiguous(
                List<Product> candidates
        ) {

            return new Resolution(
                    OwnerChatProductMatchState
                            .AMBIGUOUS_PRODUCT,
                    null,
                    candidates
            );
        }

        public static Resolution matched(
                Product product
        ) {

            return new Resolution(
                    OwnerChatProductMatchState
                            .MATCHED,
                    product,
                    List.of()
            );
        }
    }
}