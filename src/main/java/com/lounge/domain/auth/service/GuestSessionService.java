package com.lounge.domain.auth.service;

import com.lounge.domain.auth.dto.response.GuestSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestSessionService {

    private static final String GUEST_SESSION_KEY_PREFIX = "auth:guest-session:";
    private static final String ACTIVE_VALUE = "ACTIVE";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${guest-session.expiration}")
    private Long guestSessionExpiration;

    public GuestSessionResponse issueOrRefresh(String guestSessionId) {
        String resolvedSessionId = resolveSessionId(guestSessionId);

        stringRedisTemplate.opsForValue().set(
                GUEST_SESSION_KEY_PREFIX + resolvedSessionId,
                ACTIVE_VALUE,
                Duration.ofMillis(guestSessionExpiration)
        );

        return GuestSessionResponse.of(resolvedSessionId, guestSessionExpiration / 1000);
    }

    public boolean exists(String guestSessionId) {
        if (!StringUtils.hasText(guestSessionId)) {
            return false;
        }
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(GUEST_SESSION_KEY_PREFIX + guestSessionId)
        );
    }

    public void delete(String guestSessionId) {
        if (!StringUtils.hasText(guestSessionId)) {
            return;
        }

        stringRedisTemplate.delete(GUEST_SESSION_KEY_PREFIX + guestSessionId);
    }

    private String resolveSessionId(String guestSessionId) {
        if (!StringUtils.hasText(guestSessionId)) {
            return UUID.randomUUID().toString();
        }

        Boolean exists = stringRedisTemplate.hasKey(GUEST_SESSION_KEY_PREFIX + guestSessionId);
        if (Boolean.TRUE.equals(exists)) {
            return guestSessionId;
        }

        return UUID.randomUUID().toString();
    }
}
