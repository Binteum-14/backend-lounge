package com.lounge.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GuestSessionResponse {

    private String guestSessionId;
    private Long expiresInSeconds;

    public static GuestSessionResponse of(String guestSessionId, Long expiresInSeconds) {
        return new GuestSessionResponse(guestSessionId, expiresInSeconds);
    }
}
