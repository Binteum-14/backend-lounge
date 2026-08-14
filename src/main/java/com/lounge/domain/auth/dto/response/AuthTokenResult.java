package com.lounge.domain.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthTokenResult {

    private Long userId;
    private String accessToken;
    private String refreshToken;

    public static AuthTokenResult of(Long userId, String accessToken, String refreshToken) {
        return new AuthTokenResult(userId, accessToken, refreshToken);
    }

    public TokenResponse toTokenResponse() {
        return TokenResponse.bearer(userId, accessToken);
    }
}
