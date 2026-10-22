package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 유효한 자격증명을 제공하지 않고 접근하려 할때 401 에러
        ApiResponseWriter.write(response, objectMapper, ErrorCode.UNAUTHORIZED);
    }
}
