package com.lounge.domain.auth.service;

import com.lounge.domain.auth.dto.request.LoginRequest;
import com.lounge.domain.auth.dto.request.SignupRequest;
import com.lounge.domain.auth.dto.response.AuthTokenResult;
import com.lounge.domain.auth.dto.response.UsernameCheckResponse;
import com.lounge.domain.auth.exception.code.AuthErrorCode;
import com.lounge.domain.user.entity.User;
import com.lounge.domain.user.repository.UserRepository;
import com.lounge.global.exception.GeneralException;
import com.lounge.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public UsernameCheckResponse checkUsername(String username) {
        boolean available = !userRepository.existsByUsername(username);
        return UsernameCheckResponse.of(available);
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw GeneralException.of(AuthErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.create(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(user);
    }

    public AuthTokenResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> GeneralException.of(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw GeneralException.of(AuthErrorCode.INVALID_CREDENTIALS);
        }

        return createAuthTokenResult(user);
    }

    public AuthTokenResult reissue(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw GeneralException.of(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        jwtProvider.validateRefreshToken(refreshToken);

        Long userId = jwtProvider.getUserId(refreshToken);
        String tokenId = jwtProvider.getTokenId(refreshToken);

        refreshTokenService.validateStoredToken(userId, tokenId, refreshToken);
        refreshTokenService.delete(userId, tokenId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(AuthErrorCode.INVALID_REFRESH_TOKEN));

        return createAuthTokenResult(user);
    }

    private AuthTokenResult createAuthTokenResult(User user) {
        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = refreshTokenService.issue(user).getToken();
        return AuthTokenResult.of(user.getId(), accessToken, refreshToken);
    }
}
