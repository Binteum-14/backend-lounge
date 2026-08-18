package com.lounge.domain.visitpass.dto.request;

import jakarta.validation.constraints.NotNull;

public record VisitPassIssueRequest(
        @NotNull(message = "recommendationProductId는 필수입니다.")
        Long recommendationProductId
) {
}
