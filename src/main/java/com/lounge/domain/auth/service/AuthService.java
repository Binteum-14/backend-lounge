package com.lounge.domain.auth.service;

import com.lounge.domain.auth.dto.request.LoginRequest;
import com.lounge.domain.auth.dto.request.SignupRequest;
import com.lounge.domain.auth.dto.response.AuthTokenResult;
import com.lounge.domain.auth.dto.response.UsernameCheckResponse;
import com.lounge.domain.auth.exception.code.AuthErrorCode;
import com.lounge.domain.diagnosis.repository.DiagnosisRepository;
import com.lounge.domain.flightfocusdetail.repository.FlightFocusDetailRepository;
import com.lounge.domain.focusrecord.repository.FocusRecordRepository;
import com.lounge.domain.ownerproduct.repository.OwnedProductRepository;
import com.lounge.domain.recommendation.repository.RecommendationRepository;
import com.lounge.domain.user.entity.User;
import com.lounge.domain.user.repository.UserRepository;
import com.lounge.domain.visitpass.repository.VisitPassRepository;
import com.lounge.domain.visitpassproduct.repository.VisitPassProductRepository;
import com.lounge.global.api.code.GeneralErrorCode;
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
    private final FlightFocusDetailRepository flightFocusDetailRepository;
    private final OwnedProductRepository ownedProductRepository;
    private final FocusRecordRepository focusRecordRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final RecommendationRepository recommendationRepository;
    private final VisitPassProductRepository visitPassProductRepository;
    private final VisitPassRepository visitPassRepository;

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

    public void logout(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }

        try {
            jwtProvider.validateRefreshToken(refreshToken);

            Long userId = jwtProvider.getUserId(refreshToken);
            String tokenId = jwtProvider.getTokenId(refreshToken);

            refreshTokenService.delete(userId, tokenId);
        } catch (GeneralException | IllegalArgumentException ignored) {
            // 이미 만료됐거나, 꺠졌거나, 이상한 값이어도 에러 무시
        }
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        ownedProductRepository.deleteByUser_Id(userId);
        flightFocusDetailRepository.deleteByFocusRecord_User_Id(userId);
        focusRecordRepository.deleteByUser_Id(userId);
        recommendationRepository.detachUser(userId);
        diagnosisRepository.detachUser(userId);
        visitPassProductRepository.deleteByVisitPass_User_Id(userId);
        visitPassRepository.deleteByUser_Id(userId);
        userRepository.delete(user);
        userRepository.flush();
        refreshTokenService.deleteAllByUserId(userId);
    }

    private AuthTokenResult createAuthTokenResult(User user) {
        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = refreshTokenService.issue(user).getToken();
        return AuthTokenResult.of(user.getId(), accessToken, refreshToken);
    }
}
