package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void responseStatusException_preservesForbiddenStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/weekly-menu");
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");

        ResponseEntity<ErrorResponse> response =
                handler.handleResponseStatusException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("HTTP_403");
        assertThat(response.getBody().getDetail()).isEqualTo("관리자 권한이 필요합니다.");
    }
}
