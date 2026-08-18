package com.lounge.domain.presence.service;

import com.lounge.domain.auth.dto.response.GuestSessionResponse;
import com.lounge.domain.auth.service.GuestSessionService;
import com.lounge.domain.focusrecord.entity.FocusThemeType;
import com.lounge.domain.presence.dto.response.PresenceHeartbeatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private static final String PRESENCE_KEY_PREFIX = "presence:";
    private static final String ACTIVE_VALUE = "ACTIVE";

    private final StringRedisTemplate stringRedisTemplate;
    private final GuestSessionService guestSessionService;

    @Value("${presence.expiration}")
    private Long presenceExpiration;

    public HeartbeatResult heartbeat(
            Long userId,
            String guestSessionId,
            FocusThemeType themeType
    ) {
        Identity identity = resolveIdentity(userId, guestSessionId);

        stringRedisTemplate.delete(presenceKey(opposite(themeType), identity));
        stringRedisTemplate.opsForValue().set(
                presenceKey(themeType, identity),
                ACTIVE_VALUE,
                Duration.ofMillis(presenceExpiration)
        );

        return new HeartbeatResult(
                new PresenceHeartbeatResponse(
                        countByPattern(PRESENCE_KEY_PREFIX + FocusThemeType.LOUNGE.name() + ":*"),
                        countByPattern(PRESENCE_KEY_PREFIX + FocusThemeType.FLIGHT.name() + ":*")
                ),
                identity.issuedGuestSession()
        );
    }

    public void deleteGuestPresence(String guestSessionId) {
        if (!StringUtils.hasText(guestSessionId)) {
            return;
        }

        Identity guestIdentity = Identity.guest(guestSessionId, null);
        stringRedisTemplate.delete(presenceKey(FocusThemeType.LOUNGE, guestIdentity));
        stringRedisTemplate.delete(presenceKey(FocusThemeType.FLIGHT, guestIdentity));
    }

    private Identity resolveIdentity(Long userId, String guestSessionId) {
        if (userId != null) {
            return Identity.user(userId);
        }

        if (guestSessionService.exists(guestSessionId)) {
            return Identity.guest(guestSessionId, null);
        }

        GuestSessionResponse issued = guestSessionService.issueOrRefresh(guestSessionId);
        return Identity.guest(issued.getGuestSessionId(), issued);
    }

    private String presenceKey(FocusThemeType themeType, Identity identity) {
        return PRESENCE_KEY_PREFIX
                + themeType.name()
                + ":"
                + identity.type()
                + ":"
                + identity.id();
    }

    private FocusThemeType opposite(FocusThemeType themeType) {
        return themeType == FocusThemeType.LOUNGE
                ? FocusThemeType.FLIGHT
                : FocusThemeType.LOUNGE;
    }

    private long countByPattern(String pattern) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(200)
                .build();

        long count = 0L;
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
        }
        return count;
    }

    public record HeartbeatResult(
            PresenceHeartbeatResponse response,
            GuestSessionResponse issuedGuestSession
    ) {
    }

    private record Identity(
            String type,
            String id,
            GuestSessionResponse issuedGuestSession
    ) {

        private static Identity user(Long userId) {
            return new Identity("user", String.valueOf(userId), null);
        }

        private static Identity guest(
                String guestSessionId,
                GuestSessionResponse issuedGuestSession
        ) {
            return new Identity("guest", guestSessionId, issuedGuestSession);
        }
    }
}
