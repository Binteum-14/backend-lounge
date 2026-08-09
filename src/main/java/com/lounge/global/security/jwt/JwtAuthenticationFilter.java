package com.lounge.global.security.jwt;

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

// 요청마다 Authorization 헤더 읽는 필터
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더 추출
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 2. Bearer 토큰이 없으면 다음 필터로 이동
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Bearer 제거 후 토큰 추출
        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        // 4. 토큰 검증
        jwtProvider.validateToken(token);

        // 5. 토큰에서 userId 추출
        Long userId = jwtProvider.getUserId(token);

        // 6. 인증 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // 7. SecurityContext 에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 8. 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}