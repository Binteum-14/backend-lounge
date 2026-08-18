package com.lounge.domain.presence.controller;

import com.lounge.domain.auth.dto.response.GuestSessionResponse;
import com.lounge.domain.presence.dto.request.PresenceHeartbeatRequest;
import com.lounge.domain.presence.dto.response.PresenceHeartbeatResponse;
import com.lounge.domain.presence.exception.code.PresenceSuccessCode;
import com.lounge.domain.presence.service.PresenceService;
import com.lounge.domain.presence.service.PresenceService.HeartbeatResult;
import com.lounge.global.api.ApiResponse;
import com.lounge.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Presence", description = "라운지/비행기 현재 이용 인원 API")
@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;
    private final CookieUtil cookieUtil;

    @Operation(
            summary = "현재 이용 heartbeat",
            description = """
                    현재 이용 중인 테마를 갱신하고 라운지/비행기 현재 인원을 반환합니다.
                    비로그인 사용자는 guestSessionId 쿠키로 식별하며,
                    쿠키가 없거나 만료되면 기존 게스트 세션을 발급합니다.
                    """
    )
    @PostMapping("/heartbeat")
    public ApiResponse<PresenceHeartbeatResponse> heartbeat(
            @AuthenticationPrincipal Long userId,
            @CookieValue(name = "guestSessionId", required = false) String guestSessionId,
            @Valid @RequestBody PresenceHeartbeatRequest request,
            HttpServletResponse response
    ) {
        HeartbeatResult result = presenceService.heartbeat(
                userId,
                guestSessionId,
                request.themeType()
        );

        addGuestSessionCookieIfIssued(response, result.issuedGuestSession());

        return ApiResponse.onSuccess(
                PresenceSuccessCode.PRESENCE_HEARTBEAT_SUCCESS,
                result.response()
        );
    }

    private void addGuestSessionCookieIfIssued(
            HttpServletResponse response,
            GuestSessionResponse issuedGuestSession
    ) {
        if (issuedGuestSession == null) {
            return;
        }

        ResponseCookie guestSessionCookie = cookieUtil.createGuestSessionCookie(
                issuedGuestSession.getGuestSessionId(),
                issuedGuestSession.getExpiresInSeconds()
        );
        response.addHeader(HttpHeaders.SET_COOKIE, guestSessionCookie.toString());
    }
}
