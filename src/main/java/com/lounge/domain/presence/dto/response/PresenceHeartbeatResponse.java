package com.lounge.domain.presence.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라운지/비행기 현재 이용 인원")
public record PresenceHeartbeatResponse(

        @Schema(description = "현재 라운지 이용 인원", example = "12")
        long loungeCount,

        @Schema(description = "현재 비행기 이용 인원", example = "5")
        long flightCount
) {
}
