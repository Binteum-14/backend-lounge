package com.lounge.domain.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenResponse {

    private Long userId;
    private String tokenType;
    private String accessToken;

    public static TokenResponse bearer(Long userId, String accessToken) {
        return new TokenResponse(userId, "Bearer", accessToken);
    }
}
