package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_003", "요청한 리소스를 찾을 수 없습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증 정보가 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_001", "메뉴를 찾을 수 없습니다."),

    // Dish
    DISH_NOT_FOUND(HttpStatus.NOT_FOUND, "DISH_001", "요리를 찾을 수 없습니다."),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001", "리뷰를 찾을 수 없습니다."),
    REVIEW_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW_002", "본인이 작성한 리뷰만 삭제할 수 있습니다."),

    // Keyword
    KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "KEYWORD_001", "키워드를 찾을 수 없습니다."),

    // Rate
    RATE_NOT_FOUND(HttpStatus.NOT_FOUND, "RATE_001", "평점 정보를 찾을 수 없습니다."),

    // Statistics
    STATISTICS_NOT_FOUND(HttpStatus.NOT_FOUND, "STATISTICS_001", "통계 정보를 찾을 수 없습니다."),

    // Prediction
    PREDICTION_NO_OBSERVATION(HttpStatus.NOT_FOUND, "PREDICTION_001", "해당 시간대의 관측 데이터가 없습니다."),
    PREDICTION_SERVER_DOWN(HttpStatus.SERVICE_UNAVAILABLE, "PREDICTION_002", "예측 서버가 응답하지 않습니다."),
    PREDICTION_FAILED(HttpStatus.BAD_GATEWAY, "PREDICTION_003", "예측 서버 호출에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
