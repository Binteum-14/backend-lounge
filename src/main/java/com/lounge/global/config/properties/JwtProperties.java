package com.lounge.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Long accessTokenExpiration,
        Long refreshTokenExpiration
) {
}
