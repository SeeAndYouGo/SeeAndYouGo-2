package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

// 컨트롤러까지 도달하지 못한 에러(없는 경로, 필터 예외 등)는 /error 로 포워딩된다.
// Spring 기본 에러 JSON 대신 공통 응답 포맷으로 내려준다.
@RestController
public class ApiErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Void>> handleError(HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.from(resolveStatus(request));

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    private HttpStatus resolveStatus(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = statusCode instanceof Integer ? HttpStatus.resolve((Integer) statusCode) : null;

        // 에러 포워딩 없이 /error 를 직접 호출한 경우
        return status != null ? status : HttpStatus.NOT_FOUND;
    }
}
