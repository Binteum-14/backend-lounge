package com.lounge.domain.auth.controller;

import com.lounge.domain.auth.dto.request.LoginRequest;
import com.lounge.domain.auth.dto.request.SignupRequest;
import com.lounge.domain.auth.dto.response.TokenResponse;
import com.lounge.domain.auth.dto.response.UsernameCheckResponse;
import com.lounge.domain.auth.exception.code.AuthSuccessCode;
import com.lounge.domain.auth.service.AuthService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
        @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, authService.login(request));
    }
}
