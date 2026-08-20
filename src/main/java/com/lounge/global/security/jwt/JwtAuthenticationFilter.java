package com.lounge.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.global.api.ApiResponse;
import com.lounge.global.exception.GeneralException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER =
            "Authorization";

    private static final String BEARER_PREFIX =
            "Bearer ";

    private static final String LOGIN_PATH = "/api/auth/login";

    private static final String SIGNUP_PATH = "/api/auth/signup";

    private final JwtProvider jwtProvider;

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return LOGIN_PATH.equals(path) || SIGNUP_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        AUTHORIZATION_HEADER
                );

        /*
         * 토큰 자체가 없는 경우:
         * Spring Security 다음 필터로 넘김
         */
        if (authorizationHeader == null
                || !authorizationHeader
                .startsWith(
                        BEARER_PREFIX
                )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader.substring(
                        BEARER_PREFIX.length()
                );

        try {

            /*
             * 핵심:
             * validateToken이 아니라
             * validateAccessToken을 사용
             */
            jwtProvider.validateAccessToken(
                    token
            );

            Long userId =
                    jwtProvider.getUserId(
                            token
                    );

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_USER"
                                    )
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (GeneralException exception) {

            /*
             * 잘못된 JWT가 들어왔으면
             * SecurityContext에 인증 정보가 남지 않도록 초기화
             */
            SecurityContextHolder
                    .clearContext();

            response.setStatus(
                    exception.getReason()
                            .getHttpStatus()
                            .value()
            );

            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    ApiResponse.onFailure(
                            exception.getCode()
                    )
            );
        }
    }
}