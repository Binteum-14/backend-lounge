package com.lounge.domain.ownerchat.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OwnerChatRequest(

        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 500, message = "질문은 500자 이하여야 합니다.")
        String message,

        @NotNull(message = "대화 기록은 필수입니다. 기록이 없으면 빈 배열을 보내주세요.")
        @Size(max = 12, message = "대화 기록은 최근 12개까지만 보낼 수 있습니다.")
        List<@Valid HistoryMessage> history

) {

    public record HistoryMessage(

            @NotNull(message = "대화 역할은 필수입니다.")
            Role role,

            @NotBlank(message = "대화 내용은 비어 있을 수 없습니다.")
            @Size(max = 1200, message = "대화 내용은 한 건당 1200자 이하여야 합니다.")
            String content
    ) {
    }

    public enum Role {
        USER,
        ASSISTANT
    }
}
