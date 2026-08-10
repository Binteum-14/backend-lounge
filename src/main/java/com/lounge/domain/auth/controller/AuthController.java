package com.lounge.domain.auth.controller;

import com.lounge.domain.auth.dto.request.LoginRequest;
import com.lounge.domain.auth.dto.request.SignupRequest;
import com.lounge.domain.auth.dto.response.AuthTokenResult;
import com.lounge.domain.auth.dto.response.TokenResponse;
import com.lounge.domain.auth.dto.response.UsernameCheckResponse;
import com.lounge.domain.auth.exception.code.AuthSuccessCode;
import com.lounge.domain.auth.service.AuthService;
import com.lounge.global.api.ApiResponse;
import com.lounge.global.config.properties.JwtProperties;
import com.lounge.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로컬 인증 API")
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final JwtProperties jwtProperties;

    @Operation(summary = "username 중복 확인", description = "회원가입 전에 username 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-username")
    public ApiResponse<UsernameCheckResponse> checkUsername(
            @NotBlank(message = "username은 필수입니다.")
            @RequestParam String username
    ) {
        return ApiResponse.onSuccess(
                AuthSuccessCode.USERNAME_CHECK_SUCCESS,
                authService.checkUsername(username)
        );
    }

    @Operation(summary = "회원가입", description = "username 중복을 확인하고 BCrypt로 password를 저장합니다.")
    @PostMapping("/signup")
    public ApiResponse<Void> signup(
        @Valid @RequestBody SignupRequest request
    ) {
        authService.signup(request);
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, null);
    }

    @Operation(summary = "로그인", description = "username/password를 검증하고 access token을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthTokenResult result = authService.login(request);
        addRefreshTokenCookie(response, result.getRefreshToken());

        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, result.toTokenResponse());
    }

    @Operation(summary = "토큰 재발급", description = "refresh token cookie를 검증하고 access token을 재발급합니다.")
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthTokenResult result = authService.reissue(refreshToken);
        addRefreshTokenCookie(response, result.getRefreshToken());

        return ApiResponse.onSuccess(AuthSuccessCode.TOKEN_REISSUE_SUCCESS, result.toTokenResponse());
    }

    @Operation(summary = "로그아웃", description = "refresh token을 Redis에서 삭제하고 refresh token cookie를 만료시킵니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        deleteRefreshTokenCookie(response);

        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }

    @Operation(summary = "회원 탈퇴", description = "인증된 사용자를 삭제하고 refresh token cookie를 만료시킵니다.")
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response
    ) {
        authService.withdraw(userId);
        deleteRefreshTokenCookie(response);

        return ApiResponse.onSuccess(AuthSuccessCode.WITHDRAW_SUCCESS, null);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
                refreshToken,
                jwtProperties.refreshTokenExpiration() / 1000
        );
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        // Max-Age=0 인 쿠키를 만들어서 브라우저가 해당 쿠키 바로 삭제하도록
        ResponseCookie refreshTokenCookie = cookieUtil.deleteRefreshTokenCookie();
        // 그걸 Set-Cookie 헤더를 응답에 넣기
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
