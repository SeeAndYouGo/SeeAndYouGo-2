package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException e,
            HttpServletRequest request) {
        logError(request, e.getErrorCode().getCode(), e.getMessage(), e);

        return buildErrorResponse(e.getErrorCode(), getMessageOrDefault(e.getMessage(), e.getErrorCode()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(
            EntityNotFoundException e,
            HttpServletRequest request) {
        logError(request, e.getErrorCode().getCode(), e.getDetail(), e);

        return buildErrorResponse(e.getErrorCode(), e.getErrorCode().getMessage());
    }

    @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleJpaEntityNotFoundException(
            javax.persistence.EntityNotFoundException e,
            HttpServletRequest request) {
        logError(request, ErrorCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), e);

        return buildErrorResponse(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {
        logError(request, ErrorCode.INVALID_INPUT_VALUE.getCode(), e.getMessage(), e);

        return buildErrorResponse(
                ErrorCode.INVALID_INPUT_VALUE,
                getMessageOrDefault(e.getMessage(), ErrorCode.INVALID_INPUT_VALUE)
        );
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestException(
            Exception e,
            HttpServletRequest request) {
        logError(request, ErrorCode.INVALID_INPUT_VALUE.getCode(), e.getMessage(), e);

        return buildErrorResponse(ErrorCode.INVALID_INPUT_VALUE, ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @ExceptionHandler({
            FileNotFoundException.class,
            NoSuchFileException.class,
            NoHandlerFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            Exception e,
            HttpServletRequest request) {
        logError(request, ErrorCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), e);

        return buildErrorResponse(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(
            ResponseStatusException e,
            HttpServletRequest request) {
        ErrorCode errorCode = resolveErrorCode(e.getStatus());
        logError(request, errorCode.getCode(), e.getReason(), e);

        return buildErrorResponse(errorCode, errorCode.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e,
            HttpServletRequest request) {
        logError(request, ErrorCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage(), e);

        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), message));
    }

    private ErrorCode resolveErrorCode(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return ErrorCode.UNAUTHORIZED;
        }
        if (status == HttpStatus.FORBIDDEN) {
            return ErrorCode.ACCESS_DENIED;
        }
        if (status == HttpStatus.NOT_FOUND) {
            return ErrorCode.RESOURCE_NOT_FOUND;
        }
        if (status.is4xxClientError()) {
            return ErrorCode.INVALID_INPUT_VALUE;
        }
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }

    private String getMessageOrDefault(String message, ErrorCode errorCode) {
        return message == null || message.trim().isEmpty()
                ? errorCode.getMessage()
                : message;
    }

    private void logError(HttpServletRequest request, String code, String detail, Exception e) {
        log.error("[{}] {} - {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                code,
                detail,
                e);
    }
}
