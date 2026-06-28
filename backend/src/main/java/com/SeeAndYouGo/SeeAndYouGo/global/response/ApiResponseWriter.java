package com.SeeAndYouGo.SeeAndYouGo.global.response;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ApiResponseWriter {
    private ApiResponseWriter() {
    }

    public static void write(HttpServletResponse response,
                             ObjectMapper objectMapper,
                             ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error(errorCode.getCode(), errorCode.getMessage())
        );
    }
}
