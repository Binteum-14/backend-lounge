package com.lounge.domain.diagnosis.scoring;

import com.lounge.domain.diagnosis.DiagnosisQuestionCatalog.ResolvedAnswer;
import com.lounge.domain.diagnosis.exception.DiagnosisException;
import com.lounge.domain.diagnosis.exception.code.DiagnosisErrorCode;

import java.util.List;
import java.util.Map;

public final class DiagnosisWeightTable {

    private static final Map<String, Map<String, WeightRule>> RULES = Map.of(
            "BARRIER", Map.of(
                    "PRICE", rule(1, 3, 0, 1, 0, 0),
                    "FIT", rule(1, 3, 1, 1, 0, 0),
                    "COMPARE", rule(0, 2, 1, 1, 0, 0)
            ),
            "PRICE_INFLUENCE", Map.of(
                    "LV1", priceRule(0, 0, 0, 0, 0, 0, 0),
                    "LV2", priceRule(0, 1, 0, 0, 0, 0, 0),
                    "LV3", priceRule(1, 1, 0, 0, 0, 0, 3),
                    "LV4", priceRule(1, 2, 0, 1, 0, 0, 6),
                    "LV5", priceRule(2, 3, 0, 1, 0, 0, 10)
            ),
            "USAGE_CONCERN", Map.of(
                    "LV1", rule(0, 0, 0, 0, 0, 0),
                    "LV2", rule(0, 1, 0, 0, 0, 0),
                    "LV3", rule(1, 2, 0, 0, 0, 0),
                    "LV4", rule(2, 3, 1, 1, 0, 0),
                    "LV5", rule(3, 3, 1, 1, 0, 0)
            ),
            "TRAVEL_FREQUENCY", Map.of(
                    "MONTHLY", rule(1, 1, 3, 0, 0, 3),
                    "EVERY_2_3_MONTHS", rule(1, 1, 2, 0, 0, 2),
                    "FEW_PER_YEAR", rule(0, 1, 1, 1, 0, 1),
                    "RARELY", rule(1, 1, 0, 2, 0, 0)
            ),
            "OUTING_FREQUENCY", Map.of(
                    "EVERYDAY", rule(3, 2, 0, 3, 1, 0),
                    "THREE_TO_FIVE", rule(2, 2, 0, 2, 1, 0),
                    "ONE_TO_TWO", rule(1, 1, 0, 1, 0, 0),
                    "SPECIAL", rule(0, 1, 1, 0, 0, 0)
            ),
            "MAIN_PLACE", Map.of(
                    "SCHOOL_OFFICE", laptopRule(2, 1, 0, 3, 3, 0),
                    "HOME", rule(1, 2, 0, 0, 0, 0),
                    "OUTDOOR", rule(1, 2, 2, 0, 0, 1),
                    "TRANSIT", rule(2, 1, 0, 3, 1, 0)
            ),
            "LUXURY_MOMENT", Map.of(
                    "COMMUTE", laptopRule(2, 1, 0, 3, 2, 0),
                    "MEETING", rule(1, 3, 0, 1, 0, 0),
                    "TRAVEL", rule(1, 1, 3, 0, 0, 3),
                    "ATTRACTION", rule(0, 2, 2, 0, 0, 1)
            )
    );

    private DiagnosisWeightTable() {
    }

    public static DerivedProfile derive(List<ResolvedAnswer> answers) {
        AxisWeights weights = AxisWeights.ZERO;
        boolean laptopRequired = false;
        int priceBonusMax = 0;

        for (ResolvedAnswer answer : answers) {
            WeightRule rule = lookup(answer.questionCode(), answer.answerCode());
            weights = weights.plus(rule.weights());
            laptopRequired = laptopRequired || rule.laptopRequired();
            priceBonusMax = Math.max(priceBonusMax, rule.priceBonusMax());
        }

        return new DerivedProfile(weights, laptopRequired, priceBonusMax);
    }

    private static WeightRule lookup(String questionCode, String answerCode) {
        Map<String, WeightRule> byAnswer = RULES.get(questionCode);
        if (byAnswer == null) {
            throw DiagnosisException.of(DiagnosisErrorCode.UNKNOWN_ANSWER);
        }
        WeightRule rule = byAnswer.get(answerCode);
        if (rule == null) {
            throw DiagnosisException.of(DiagnosisErrorCode.UNKNOWN_ANSWER);
        }
        return rule;
    }

    private static WeightRule rule(
            int storage,
            int versatility,
            int travel,
            int commute,
            int laptop,
            int cabin
    ) {
        return new WeightRule(
                new AxisWeights(storage, versatility, travel, commute, laptop, cabin),
                false,
                0
        );
    }

    private static WeightRule laptopRule(
            int storage,
            int versatility,
            int travel,
            int commute,
            int laptop,
            int cabin
    ) {
        return new WeightRule(
                new AxisWeights(storage, versatility, travel, commute, laptop, cabin),
                true,
                0
        );
    }

    private static WeightRule priceRule(
            int storage,
            int versatility,
            int travel,
            int commute,
            int laptop,
            int cabin,
            int priceBonusMax
    ) {
        return new WeightRule(
                new AxisWeights(storage, versatility, travel, commute, laptop, cabin),
                false,
                priceBonusMax
        );
    }

    public record DerivedProfile(
            AxisWeights weights,
            boolean laptopRequired,
            int priceBonusMax
    ) {
    }

    private record WeightRule(
            AxisWeights weights,
            boolean laptopRequired,
            int priceBonusMax
    ) {
    }
}
