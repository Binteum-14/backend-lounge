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
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";

    /*
     * local 환경에서는 Redis를 설치하지 않아도 되도록
     * Refresh Token을 메모리에 저장합니다.
     *
     * 서버를 재시작하면 메모리 데이터가 사라지는 것은
     * 로컬 개발 환경에서는 정상적인 동작입니다.
     */
    private final Map<String, LocalRefreshToken> localRefreshTokens =
            new ConcurrentHashMap<>();

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final TokenHashUtil tokenHashUtil;
    private final Environment environment;

    public IssuedRefreshToken issue(User user) {

        String tokenId =
                UUID.randomUUID().toString();

        String refreshToken =
                jwtProvider.createRefreshToken(
                        user,
                        tokenId
                );

        String key =
                createKey(
                        user.getId(),
                        tokenId
                );

        String hashedToken =
                tokenHashUtil.hash(
                        refreshToken
                );

        /*
         * local 환경:
         * Redis 대신 서버 메모리에 저장
         */
        if (isLocalProfile()) {

            long expiresAt =
                    System.currentTimeMillis()
                            + jwtProperties.refreshTokenExpiration();

            localRefreshTokens.put(
                    key,
                    new LocalRefreshToken(
                            hashedToken,
                            expiresAt
                    )
            );

        } else {

            /*
             * prod 환경:
             * 기존 Redis 사용
             */
            stringRedisTemplate.opsForValue().set(
                    key,
                    hashedToken,
                    Duration.ofMillis(
                            jwtProperties.refreshTokenExpiration()
                    )
            );
        }

        return IssuedRefreshToken.of(
                refreshToken
        );
    }

    public void validateStoredToken(
            Long userId,
            String tokenId,
            String refreshToken
    ) {

        String key =
                createKey(
                        userId,
                        tokenId
                );

        String storedTokenHash;

        /*
         * local 환경에서는 메모리에서 확인
         */
        if (isLocalProfile()) {

            LocalRefreshToken storedToken =
                    localRefreshTokens.get(
                            key
                    );

            /*
             * 저장된 Refresh Token이 없음
             */
            if (storedToken == null) {

                throw GeneralException.of(
                        AuthErrorCode.INVALID_REFRESH_TOKEN
                );
            }

            /*
             * 만료된 Refresh Token
             */
            if (storedToken.expiresAt()
                    < System.currentTimeMillis()) {

                localRefreshTokens.remove(
                        key
                );

                throw GeneralException.of(
                        AuthErrorCode.INVALID_REFRESH_TOKEN
                );
            }

            storedTokenHash =
                    storedToken.hashedToken();

        } else {

            /*
             * prod 환경에서는 Redis 조회
             */
            storedTokenHash =
                    stringRedisTemplate
                            .opsForValue()
                            .get(key);
        }

        String requestedTokenHash =
                tokenHashUtil.hash(
                        refreshToken
                );

        if (storedTokenHash == null
                || !requestedTokenHash.equals(
                storedTokenHash
        )) {

            throw GeneralException.of(
                    AuthErrorCode.INVALID_REFRESH_TOKEN
            );
        }
    }

    public void delete(
            Long userId,
            String tokenId
    ) {

        String key =
                createKey(
                        userId,
                        tokenId
                );

        if (isLocalProfile()) {

            localRefreshTokens.remove(
                    key
            );

            return;
        }

        stringRedisTemplate.delete(
                key
        );
    }

    public void deleteAllByUserId(
            Long userId
    ) {

        String prefix =
                REFRESH_TOKEN_KEY_PREFIX
                        + userId
                        + ":";

        /*
         * local 메모리 삭제
         */
        if (isLocalProfile()) {

            localRefreshTokens
                    .keySet()
                    .removeIf(
                            key ->
                                    key.startsWith(
                                            prefix
                                    )
                    );

            return;
        }

        /*
         * prod Redis 삭제
         */
        Set<String> keys =
                stringRedisTemplate.keys(
                        prefix + "*"
                );

        if (keys == null
                || keys.isEmpty()) {

            return;
        }

        stringRedisTemplate.delete(
                keys
        );
    }

    private String createKey(
            Long userId,
            String tokenId
    ) {

        return REFRESH_TOKEN_KEY_PREFIX
                + userId
                + ":"
                + tokenId;
    }

    /*
     * 현재 Spring Profile이 local인지 확인합니다.
     *
     * application.yml에서 default profile이 local이므로
     * 평소 로컬 실행 시 자동으로 true가 됩니다.
     */
    private boolean isLocalProfile() {

        return environment.acceptsProfiles(
                Profiles.of("local")
        );
    }

    /*
     * local 환경에서 Refresh Token 해시와
     * 만료 시간을 함께 저장합니다.
     */
    private record LocalRefreshToken(
            String hashedToken,
            long expiresAt
    ) {
    }

    @Getter
    @AllArgsConstructor(
            access = AccessLevel.PRIVATE
    )
    public static class IssuedRefreshToken {

        private String token;

        public static IssuedRefreshToken of(
                String token
        ) {

            return new IssuedRefreshToken(
                    token
            );
        }
    }
}