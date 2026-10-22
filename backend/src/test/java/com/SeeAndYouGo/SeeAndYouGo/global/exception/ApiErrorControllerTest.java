package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.RequestDispatcher;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorControllerTest {

    private final ApiErrorController controller = new ApiErrorController();

    @Test
    void notFound_returnsResourceNotFound() {
        ResponseEntity<ApiResponse<Void>> response = controller.handleError(errorRequest(404));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("COMMON_003");
        assertThat(response.getBody().getMessage()).isEqualTo("요청한 리소스를 찾을 수 없습니다.");
    }

    @Test
    void serverError_returnsInternalServerErrorWithoutDetail() {
        ResponseEntity<ApiResponse<Void>> response = controller.handleError(errorRequest(500));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("COMMON_001");
        assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }

    @Test
    void directCallWithoutErrorStatus_returnsResourceNotFound() {
        ResponseEntity<ApiResponse<Void>> response = controller.handleError(new MockHttpServletRequest("GET", "/error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("COMMON_003");
    }

    private MockHttpServletRequest errorRequest(int status) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
        return request;
    }
}
