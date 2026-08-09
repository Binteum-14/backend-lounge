package com.lounge.domain.auth.service;

import com.lounge.domain.auth.dto.request.LoginRequest;
import com.lounge.domain.auth.dto.request.SignupRequest;
import com.lounge.domain.auth.dto.response.TokenResponse;
import com.lounge.domain.auth.exception.code.AuthErrorCode;
import com.lounge.domain.user.entity.User;
import com.lounge.domain.user.repository.UserRepository;
import com.lounge.global.exception.GeneralException;
import com.lounge.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw GeneralException.of(AuthErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.create(
                request.username(),
                passwordEncoder.encode(request.password())
        );
        User savedUser = userRepository.save(user);

        return createTokenResponse(savedUser);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> GeneralException.of(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw GeneralException.of(AuthErrorCode.INVALID_CREDENTIALS);
        }

        return createTokenResponse(user);
    }

    private TokenResponse createTokenResponse(User user) {
        String accessToken = jwtProvider.createAccessToken(user);
        return TokenResponse.bearer(user.getId(), accessToken);
    }
}
