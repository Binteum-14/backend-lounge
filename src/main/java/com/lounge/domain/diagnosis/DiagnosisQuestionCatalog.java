package com.lounge.domain.diagnosis;

import com.lounge.domain.diagnosis.exception.DiagnosisException;
import com.lounge.domain.diagnosis.exception.code.DiagnosisErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DiagnosisQuestionCatalog {

    private static final List<Question> QUESTION_LIST = List.of(
            q(1, "BARRIER", "럭셔리 구매를 망설이는 가장 큰 이유가 무엇인가요?",
                    a(1, "PRICE", "가격이 부담된다"),
                    a(2, "FIT", "나한테 맞을지 모르겠다"),
                    a(3, "COMPARE", "다른 브랜드와 비교 중이다")
            ),
            q(2, "PRICE_INFLUENCE", "가격이 구매 결정에 얼마나 영향을 주나요?",
                    a(1, "LV1", "전혀 영향 없음"),
                    a(2, "LV2", "영향 있는 편"),
                    a(3, "LV3", "보통"),
                    a(4, "LV4", "영향이 큰 편"),
                    a(5, "LV5", "매우 큰 영향")
            ),
            q(3, "USAGE_CONCERN", "구매 전 제품 활용성을 얼마나 고민하시나요?",
                    a(1, "LV1", "전혀 고민하지 않음"),
                    a(2, "LV2", "별로 고민하지 않음"),
                    a(3, "LV3", "보통"),
                    a(4, "LV4", "고민하는 편"),
                    a(5, "LV5", "매우 고민함")
            ),
            q(4, "TRAVEL_FREQUENCY", "평소 여행을 얼마나 자주 떠나는 편인가요?",
                    a(1, "MONTHLY", "한 달에 한 번 이상"),
                    a(2, "EVERY_2_3_MONTHS", "2~3개월에 한 번"),
                    a(3, "FEW_PER_YEAR", "일 년에 몇 번"),
                    a(4, "RARELY", "거의 안 감")
            ),
            q(5, "OUTING_FREQUENCY", "일주일에 외출은 어느 정도 하나요?",
                    a(1, "EVERYDAY", "거의 매일"),
                    a(2, "THREE_TO_FIVE", "주 3~5회"),
                    a(3, "ONE_TO_TWO", "주 1~2회"),
                    a(4, "SPECIAL", "특별한 날에만")
            ),
            q(6, "MAIN_PLACE", "평소 가장 많은 시간을 보내는 곳은 어디인가요?",
                    a(1, "SCHOOL_OFFICE", "학교/직장"),
                    a(2, "HOME", "집"),
                    a(3, "OUTDOOR", "야외 활동"),
                    a(4, "TRANSIT", "이동 시간(지하철 등)")
            ),
            q(7, "LUXURY_MOMENT", "럭셔리 제품을 많이 활용하는 순간은 언제인가요?",
                    a(1, "COMMUTE", "출퇴근"),
                    a(2, "MEETING", "약속/모임"),
                    a(3, "TRAVEL", "여행/특별한 행사"),
                    a(4, "ATTRACTION", "매력적인 장소 방문")
            )
    );

    private static final Map<Integer, Question> QUESTIONS = QUESTION_LIST.stream()
            .collect(Collectors.toUnmodifiableMap(Question::questionNo, Function.identity()));

    public static final Set<Integer> REQUIRED_QUESTION_NOS = Set.copyOf(QUESTIONS.keySet());

    private DiagnosisQuestionCatalog() {
    }

    public static ResolvedAnswer resolve(Integer questionNo, Integer answerNo) {
        if (questionNo == null || answerNo == null) {
            throw DiagnosisException.of(DiagnosisErrorCode.UNKNOWN_ANSWER);
        }

        Question question = QUESTIONS.get(questionNo);
        if (question == null) {
            throw DiagnosisException.of(DiagnosisErrorCode.UNKNOWN_ANSWER);
        }

        return question.answers().stream()
                .filter(option -> option.answerNo() == answerNo)
                .findFirst()
                .map(option -> new ResolvedAnswer(
                        question.questionNo(),
                        question.questionCode(),
                        question.questionText(),
                        option.answerNo(),
                        option.answerCode(),
                        option.answerText()
                ))
                .orElseThrow(() -> DiagnosisException.of(DiagnosisErrorCode.UNKNOWN_ANSWER));
    }

    public static String questionText(Integer questionNo, String questionCode) {
        Question question = QUESTIONS.get(questionNo);
        if (question != null) {
            return question.questionText();
        }
        if (questionCode == null || questionCode.isBlank()) {
            return "";
        }
        return questionCode;
    }

    public static String answerText(Integer questionNo, String answerCode, String storedAnswerText) {
        if (storedAnswerText != null && !storedAnswerText.isBlank()) {
            return storedAnswerText;
        }

        Question question = QUESTIONS.get(questionNo);
        if (question != null && answerCode != null) {
            for (AnswerOption option : question.answers()) {
                if (option.answerCode().equals(answerCode)) {
                    return option.answerText();
                }
            }
        }

        return answerCode == null ? "" : answerCode;
    }

    private static Question q(
            int questionNo,
            String questionCode,
            String questionText,
            AnswerOption... answers
    ) {
        return new Question(questionNo, questionCode, questionText, List.of(answers));
    }

    private static AnswerOption a(int answerNo, String answerCode, String answerText) {
        return new AnswerOption(answerNo, answerCode, answerText);
    }

    public record Question(
            int questionNo,
            String questionCode,
            String questionText,
            List<AnswerOption> answers
    ) {
    }

    public record AnswerOption(
            int answerNo,
            String answerCode,
            String answerText
    ) {
    }

    public record ResolvedAnswer(
            int questionNo,
            String questionCode,
            String questionText,
            int answerNo,
            String answerCode,
            String answerText
    ) {
    }
}
