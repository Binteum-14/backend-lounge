package com.lounge.domain.ownerchat.dto.response;

import com.lounge.domain.ownerchat.dto.OwnerChatProductCandidate;
import com.lounge.domain.ownerchat.dto.OwnerChatProductMatchState;

import java.util.List;

public record OwnerChatResponse(

        OwnerChatProductMatchState state,

        Long productId,

        String productName,

        String answer,

        List<OwnerChatProductCandidate> candidates,

        List<String> suggestedQuestions

) {
}