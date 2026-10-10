package com.SeeAndYouGo.SeeAndYouGo.global.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestControllerAdvice(basePackages = "com.SeeAndYouGo.SeeAndYouGo")
@RequiredArgsConstructor
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();
        return !ApiResponse.class.isAssignableFrom(parameterType)
                && !byte[].class.isAssignableFrom(parameterType)
                && !Resource.class.isAssignableFrom(parameterType)
                && !StreamingResponseBody.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof ApiResponse
                || body instanceof byte[]
                || body instanceof Resource
                || body instanceof StreamingResponseBody) {
            return body;
        }

        if (!isJsonResponse(selectedContentType, selectedConverterType)) {
            return body;
        }

        ApiResponse<Object> apiResponse = ApiResponse.success(body);

        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return writeAsString(apiResponse);
        }

        return apiResponse;
    }

    private boolean isJsonResponse(MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType) {
        return selectedContentType == null
                || MediaType.APPLICATION_JSON.includes(selectedContentType)
                || selectedContentType.includes(MediaType.APPLICATION_JSON)
                || StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType);
    }

    private String writeAsString(ApiResponse<Object> apiResponse) {
        try {
            return objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize API response", e);
        }
    }
}
