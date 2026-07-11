package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;
    private final String userMessage;

    public EntityNotFoundException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    public EntityNotFoundException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, null);
    }

    /**
     * @param detail      서버 로그 전용 식별자(email=, id= 등). 사용자에게는 노출되지 않는다.
     * @param userMessage 사용자에게 그대로 노출할 상황별 안내 문구. null이면 errorCode의 기본 메시지를 사용한다.
     */
    public EntityNotFoundException(ErrorCode errorCode, String detail, String userMessage) {
        super(errorCode.getMessage() + (detail != null ? " - " + detail : ""));
        this.errorCode = errorCode;
        this.detail = detail;
        this.userMessage = userMessage;
    }
}
