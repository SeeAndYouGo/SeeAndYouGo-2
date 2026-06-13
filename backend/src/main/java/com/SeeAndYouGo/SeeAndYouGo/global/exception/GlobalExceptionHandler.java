package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException e,
            HttpServletRequest request) {

        log.error("[{}] {} - {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getErrorCode().getCode(),
                e.getDetail(),
                e);

        ErrorResponse response = ErrorResponse.of(e.getErrorCode(), e.getDetail());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.error("[{}] {} - IllegalArgumentException: {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage(),
                e);

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException e,
            HttpServletRequest request) {
        HttpStatus status = e.getStatus();
        String detail = e.getReason();

        log.warn("[{}] {} - {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                detail);

        ErrorResponse response = ErrorResponse.builder()
                .code("HTTP_" + status.value())
                .message(status.getReasonPhrase())
                .detail(detail)
                .build();
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request) {

        log.error("[{}] {} - Unexpected error: {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage(),
                e);

        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }
}
