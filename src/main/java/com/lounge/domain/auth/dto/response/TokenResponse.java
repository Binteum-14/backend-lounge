package com.lounge.domain.auth.dto.response;

public record TokenResponse(
        Long userId,
        String tokenType,
        String accessToken
) {

    public static TokenResponse bearer(Long userId, String accessToken) {
        return new TokenResponse(userId, "Bearer", accessToken);
    }
}
