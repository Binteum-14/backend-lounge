package com.lounge.domain.auth.service;

import com.lounge.domain.auth.dto.response.GuestSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GuestSessionService {

    private static final String GUEST_SESSION_KEY_PREFIX =
            "auth:guest-session:";

    private static final String ACTIVE_VALUE =
            "ACTIVE";

    /*
     * local 환경에서는 Redis 대신
     * guest session 만료 시간을 메모리에 저장합니다.
     */
    private final Map<String, Long> localGuestSessions =
            new ConcurrentHashMap<>();

    private final StringRedisTemplate stringRedisTemplate;
    private final Environment environment;

    @Value("${guest-session.expiration}")
    private Long guestSessionExpiration;

    public GuestSessionResponse issueOrRefresh(
            String guestSessionId
    ) {

        String resolvedSessionId =
                resolveSessionId(
                        guestSessionId
                );

        /*
         * local 환경
         */
        if (isLocalProfile()) {

            long expiresAt =
                    System.currentTimeMillis()
                            + guestSessionExpiration;

            localGuestSessions.put(
                    resolvedSessionId,
                    expiresAt
            );

        } else {

            /*
             * prod 환경
             */
            stringRedisTemplate
                    .opsForValue()
                    .set(
                            GUEST_SESSION_KEY_PREFIX
                                    + resolvedSessionId,

                            ACTIVE_VALUE,

                            Duration.ofMillis(
                                    guestSessionExpiration
                            )
                    );
        }

        return GuestSessionResponse.of(
                resolvedSessionId,
                guestSessionExpiration / 1000
        );
    }

    public void delete(
            String guestSessionId
    ) {

        if (!StringUtils.hasText(
                guestSessionId
        )) {

            return;
        }

        if (isLocalProfile()) {

            localGuestSessions.remove(
                    guestSessionId
            );

            return;
        }

        stringRedisTemplate.delete(
                GUEST_SESSION_KEY_PREFIX
                        + guestSessionId
        );
    }

    private String resolveSessionId(
            String guestSessionId
    ) {

        /*
         * 기존 세션 ID가 없으면 새로 발급
         */
        if (!StringUtils.hasText(
                guestSessionId
        )) {

            return UUID.randomUUID()
                    .toString();
        }

        /*
         * local 환경
         */
        if (isLocalProfile()) {

            Long expiresAt =
                    localGuestSessions.get(
                            guestSessionId
                    );

            /*
             * 아직 유효한 기존 세션
             */
            if (expiresAt != null
                    && expiresAt
                    > System.currentTimeMillis()) {

                return guestSessionId;
            }

            /*
             * 만료된 세션이면 제거
             */
            if (expiresAt != null) {

                localGuestSessions.remove(
                        guestSessionId
                );
            }

            return UUID.randomUUID()
                    .toString();
        }

        /*
         * prod 환경에서는 기존 Redis 사용
         */
        Boolean exists =
                stringRedisTemplate.hasKey(
                        GUEST_SESSION_KEY_PREFIX
                                + guestSessionId
                );

        if (Boolean.TRUE.equals(
                exists
        )) {

            return guestSessionId;
        }

        return UUID.randomUUID()
                .toString();
    }

    private boolean isLocalProfile() {

        return environment.acceptsProfiles(
                Profiles.of("local")
        );
    }
}