package com.ddcj.binteum.global.api.handler;

import com.ddcj.binteum.global.api.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

// 성공 응답에 대해 ApiResponse 안의 HttpStatus를 실제 HTTP Status로 반영하는 핸들러
// @RestController 가 붙은 모든 컨트롤러에 자동으로 적용
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalSuccessHandler implements ResponseBodyAdvice<Object> {

    // 모든 응답에 대하여 일단 beforeBodyWrite 실행
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // body : 컨트롤러가 리턴한 값 그 자체
        // 타입 검사 -> 맞으면 자동으로 형변환
        if (body instanceof ApiResponse<?> apiResponse &&
                response instanceof ServletServerHttpResponse servletResponse) {
            if (apiResponse.getHttpStatus() != null) {
                // 실제 HTTP status 로 옮겨 적어줌
                servletResponse.setStatusCode(apiResponse.getHttpStatus());
            }
        }

        return body;
    }
}
