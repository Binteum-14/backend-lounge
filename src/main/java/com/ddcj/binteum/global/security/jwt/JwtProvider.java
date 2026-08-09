package com.ddcj.binteum.global.security.jwt;

import com.ddcj.binteum.domain.user.entity.User;
import com.ddcj.binteum.global.api.code.GeneralErrorCode;
import com.ddcj.binteum.global.config.properties.JwtProperties;
import com.ddcj.binteum.global.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private Key key;

    // JWT 서명용 키로 바꾸기
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(User user) {
        return createToken(user, jwtProperties.accessTokenExpiration());
    }

    private String createToken(User user, Long expiration) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public void validateToken(String token) {
        try {
            /* 토큰 검증
            - 서명이 맞는지
            - 만료되지 않았는지
            - 토큰 형식이 올바른지
             */
            parseClaims(token);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new GeneralException(GeneralErrorCode.TOKEN_INVALID);
        }
    }

    public Long getUserId(String token) {
        // 토큰에서 userId 추출
        String subject = parseClaims(token).getSubject();

        return Long.valueOf(subject);
    }

    private Claims parseClaims(String token) {
        // 토큰 파싱
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}