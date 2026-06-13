package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public EntityNotFoundException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + " - " + detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
