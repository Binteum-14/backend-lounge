package com.lounge.global.security.jwt;

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

        } catch (GeneralException exception) {

            SecurityContextHolder
                    .clearContext();
            request.setAttribute(
                    JwtAuthenticationEntryPoint.JWT_EXCEPTION_ATTRIBUTE,
                    exception
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}
