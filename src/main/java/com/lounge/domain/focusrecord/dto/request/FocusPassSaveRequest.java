package com.lounge.domain.focusrecord.dto.request;

import com.lounge.domain.focusrecord.entity.FocusThemeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Schema(description = "포커스 패스 저장 요청")
public class FocusPassSaveRequest {

    @Schema(
            description = "집중 테마 타입입니다. LOUNGE는 일반 라운지 집중, FLIGHT는 항공편 테마 집중입니다.",
            allowableValues = {"LOUNGE", "FLIGHT"},
            example = "FLIGHT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "themeType은 필수입니다.")
    private FocusThemeType themeType;

    @Schema(description = "전체 진행 시간입니다. 단위는 분입니다.", example = "185")
    @NotNull(message = "allMinutes는 필수입니다.")
    @Min(value = 1, message = "allMinutes는 1 이상이어야 합니다.")
    private Integer allMinutes;

    @Schema(description = "집중 시간입니다. 단위는 초입니다.", example = "10800")
    @NotNull(message = "studySeconds는 필수입니다.")
    @PositiveOrZero(message = "studySeconds는 0 이상이어야 합니다.")
    private Integer studySeconds;

    @Schema(description = "휴식 시간입니다. 단위는 초입니다.", example = "900")
    @NotNull(message = "breakSeconds는 필수입니다.")
    @PositiveOrZero(message = "breakSeconds는 0 이상이어야 합니다.")
    private Integer breakSeconds;

    @Schema(description = "집중 시작 시각입니다.", example = "2026-08-11T18:00:00")
    @NotNull(message = "startedAt은 필수입니다.")
    private LocalDateTime startedAt;

    @Schema(description = "집중 종료 시각입니다.", example = "2026-08-11T21:05:00")
    @NotNull(message = "endedAt은 필수입니다.")
    private LocalDateTime endedAt;

    @Schema(description = "항공편명입니다. themeType이 FLIGHT일 때만 필요합니다.", example = "KE888", nullable = true)
    private String flightNumber;

    @Schema(description = "출발 공항 코드입니다. themeType이 FLIGHT일 때만 필요합니다.", example = "ICN", nullable = true)
    private String departureAirport;

    @Schema(description = "도착 공항 코드입니다. themeType이 FLIGHT일 때만 필요합니다.", example = "NRT", nullable = true)
    private String arrivalAirport;

    @Schema(description = "출발 예정 시각입니다. themeType이 FLIGHT일 때만 필요합니다.", example = "18:00", nullable = true)
    private LocalTime departureTime;
}
