package com.lounge.domain.presence.dto.request;

import com.lounge.domain.focusrecord.entity.FocusThemeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = "현재 이용 테마 heartbeat 요청",
        example = """
                {
                  "themeType": "LOUNGE"
                }
                """
)
public record PresenceHeartbeatRequest(

        @Schema(
                description = "현재 이용 중인 테마입니다.",
                allowableValues = {"LOUNGE", "FLIGHT"},
                example = "LOUNGE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "themeType은 필수입니다.")
        FocusThemeType themeType
) {
}
