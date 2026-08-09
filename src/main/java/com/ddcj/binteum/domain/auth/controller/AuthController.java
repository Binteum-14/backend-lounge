package com.ddcj.binteum.domain.auth.controller;

import com.ddcj.binteum.domain.auth.dto.request.LoginRequest;
import com.ddcj.binteum.domain.auth.dto.request.SignupRequest;
import com.ddcj.binteum.domain.auth.dto.response.TokenResponse;
import com.ddcj.binteum.domain.auth.exception.code.AuthSuccessCode;
import com.ddcj.binteum.domain.auth.service.AuthService;
import com.ddcj.binteum.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로컬 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "username 중복을 확인하고 BCrypt로 password를 저장합니다.")
    @PostMapping("/signup")
    public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, authService.signup(request));
    }

    @Operation(summary = "로그인", description = "username/password를 검증하고 access token을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, authService.login(request));
    }
}
