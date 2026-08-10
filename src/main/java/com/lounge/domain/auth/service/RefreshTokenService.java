package com.lounge.domain.auth.service;

import com.lounge.domain.auth.exception.code.AuthErrorCode;
import com.lounge.domain.user.entity.User;
import com.lounge.global.config.properties.JwtProperties;
import com.lounge.global.exception.GeneralException;
import com.lounge.global.security.jwt.JwtProvider;
import com.lounge.global.util.TokenHashUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final TokenHashUtil tokenHashUtil;

    public IssuedRefreshToken issue(User user) {
        String tokenId = UUID.randomUUID().toString();
        String refreshToken = jwtProvider.createRefreshToken(user, tokenId);
        String key = createKey(user.getId(), tokenId);
        String hashedToken = tokenHashUtil.hash(refreshToken);

        stringRedisTemplate.opsForValue().set(
                key,
                hashedToken,
                Duration.ofMillis(jwtProperties.refreshTokenExpiration())
        );

        return IssuedRefreshToken.of(refreshToken);
    }

    public void validateStoredToken(Long userId, String tokenId, String refreshToken) {
        String key = createKey(userId, tokenId);
        String storedTokenHash = stringRedisTemplate.opsForValue().get(key);
        String requestedTokenHash = tokenHashUtil.hash(refreshToken);

        if (!requestedTokenHash.equals(storedTokenHash)) {
            throw GeneralException.of(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    public void delete(Long userId, String tokenId) {
        stringRedisTemplate.delete(createKey(userId, tokenId));
    }

    private String createKey(Long userId, String tokenId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId + ":" + tokenId;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IssuedRefreshToken {

        private String token;

        public static IssuedRefreshToken of(String token) {
            return new IssuedRefreshToken(token);
        }
    }
}
