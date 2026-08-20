package com.lounge.domain.ownerchat.dto;

import java.util.List;

public record AiOwnerCareResult(
        String answer,
        List<String> suggestedQuestions
) {
}
