package com.lounge.domain.diagnosis;

import java.util.Map;

public final class DiagnosisQuestionCatalog {

    private static final Map<Integer, String> QUESTIONS_BY_NO = Map.of(
            1, "럭셔리 구매를 망설이는 가장 큰 이유가 무엇인가요?",
            2, "가격이 구매 결정에 얼마나 영향을 주나요?",
            3, "구매 전 제품 활용성을 얼마나 고민하시나요?",
            4, "평소 여행을 얼마나 자주 떠나는 편인가요?",
            5, "일주일에 외출은 어느 정도 하나요?",
            6, "평소 가장 많은 시간을 보내는 곳은 어디인가요?",
            7, "럭셔리 제품을 많이 활용하는 순간은 언제인가요?"
    );

    private DiagnosisQuestionCatalog() {
    }

    public static String questionText(Integer questionNo, String questionCode) {
        if (questionNo != null && QUESTIONS_BY_NO.containsKey(questionNo)) {
            return QUESTIONS_BY_NO.get(questionNo);
        }
        if (questionCode == null || questionCode.isBlank()) {
            return "";
        }
        return questionCode;
    }
}
