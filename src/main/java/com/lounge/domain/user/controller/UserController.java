package com.lounge.domain.user.controller;

import com.lounge.domain.user.dto.response.UserMeResponse;
import com.lounge.domain.user.exception.code.UserSuccessCode;
import com.lounge.domain.user.service.UserService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 username 조회", description = "인증된 사용자의 username만 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getMe(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(UserSuccessCode.USER_ME_SUCCESS, userService.getMe(userId));
    }
}
