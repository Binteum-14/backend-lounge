package com.lounge.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.global.api.ApiResponse;
import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.api.code.GeneralErrorCode;
import com.lounge.global.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String JWT_EXCEPTION_ATTRIBUTE = "jwtException";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        BaseErrorCode errorCode = resolveErrorCode(request);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.onFailure(errorCode)
        );
    }

    private BaseErrorCode resolveErrorCode(HttpServletRequest request) {
        Object attribute = request.getAttribute(JWT_EXCEPTION_ATTRIBUTE);
        if (attribute instanceof GeneralException generalException) {
            return generalException.getCode();
        }
        return GeneralErrorCode.JWT_UNAUTHORIZED;
    }
}
