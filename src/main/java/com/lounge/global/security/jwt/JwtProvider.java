package com.lounge.global.security.jwt;

import com.lounge.domain.user.entity.User;
import com.lounge.global.api.code.GeneralErrorCode;
import com.lounge.global.config.properties.JwtProperties;
import com.lounge.global.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

    private static final String TOKEN_TYPE_CLAIM =
            "type";

    private static final String TOKEN_ID_CLAIM =
            "jti";

    private static final String ACCESS_TOKEN_TYPE =
            "ACCESS";

    private static final String REFRESH_TOKEN_TYPE =
            "REFRESH";

    private final JwtProperties jwtProperties;

    private Key key;

    @PostConstruct
    public void init() {

        this.key =
                Keys.hmacShaKeyFor(
                        jwtProperties.secret()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );
    }

    public String createAccessToken(
            User user
    ) {

        Date now =
                new Date();

        Date expiredAt =
                new Date(
                        now.getTime()
                                + jwtProperties
                                .accessTokenExpiration()
                );

        return Jwts.builder()
                .setSubject(
                        String.valueOf(
                                user.getId()
                        )
                )
                .claim(
                        TOKEN_TYPE_CLAIM,
                        ACCESS_TOKEN_TYPE
                )
                .claim(
                        "username",
                        user.getUsername()
                )
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public String createRefreshToken(
            User user,
            String tokenId
    ) {

        Date now =
                new Date();

        Date expiredAt =
                new Date(
                        now.getTime()
                                + jwtProperties
                                .refreshTokenExpiration()
                );

        return Jwts.builder()
                .setSubject(
                        String.valueOf(
                                user.getId()
                        )
                )
                .claim(
                        TOKEN_TYPE_CLAIM,
                        REFRESH_TOKEN_TYPE
                )
                .claim(
                        TOKEN_ID_CLAIM,
                        tokenId
                )
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public void validateToken(
            String token
    ) {

        try {

            parseClaims(token);

        } catch (ExpiredJwtException exception) {

            throw new GeneralException(
                    GeneralErrorCode.TOKEN_EXPIRED
            );

        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {

            throw new GeneralException(
                    GeneralErrorCode.TOKEN_INVALID
            );
        }
    }

    /*
     * 일반 보호 API에서는 반드시 ACCESS Token만 허용합니다.
     */
    public void validateAccessToken(
            String token
    ) {

        validateToken(token);

        String tokenType =
                parseClaims(token)
                        .get(
                                TOKEN_TYPE_CLAIM,
                                String.class
                        );

        if (!ACCESS_TOKEN_TYPE.equals(
                tokenType
        )) {

            throw new GeneralException(
                    GeneralErrorCode.TOKEN_INVALID
            );
        }
    }

    public void validateRefreshToken(
            String token
    ) {

        validateToken(token);

        String tokenType =
                parseClaims(token)
                        .get(
                                TOKEN_TYPE_CLAIM,
                                String.class
                        );

        if (!REFRESH_TOKEN_TYPE.equals(
                tokenType
        )) {

            throw new GeneralException(
                    GeneralErrorCode.TOKEN_INVALID
            );
        }
    }

    public Long getUserId(
            String token
    ) {

        String subject =
                parseClaims(token)
                        .getSubject();

        return Long.valueOf(
                subject
        );
    }

    public String getTokenId(
            String token
    ) {

        return parseClaims(token)
                .get(
                        TOKEN_ID_CLAIM,
                        String.class
                );
    }

    private Claims parseClaims(
            String token
    ) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}