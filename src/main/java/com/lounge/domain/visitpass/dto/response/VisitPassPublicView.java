package com.lounge.domain.visitpass.dto.response;

import java.time.LocalDate;
import java.util.List;

public record VisitPassPublicView(
        String username,
        LocalDate diagnosedAt,
        String resultSummary,
        List<AnswerView> answers,
        List<ProductView> products
) {

    public record AnswerView(
            Integer questionNo,
            String questionCode,
            String answerText
    ) {
    }

    public record ProductView(
            Integer rank,
            String name,
            String imageUrl,
            String description,
            String recommendationReason
    ) {
    }
}
