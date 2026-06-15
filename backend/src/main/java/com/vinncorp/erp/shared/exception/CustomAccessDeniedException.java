package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class CustomAccessDeniedException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomAccessDeniedException() {
        super("Access Denied");
        this.errorCode = ErrorCode.FORBIDDEN;
    }

    public CustomAccessDeniedException(String message) {
        super(message);
        this.errorCode = ErrorCode.FORBIDDEN;
    }

    public CustomAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.FORBIDDEN;
    }

    public CustomAccessDeniedException(Throwable cause) {
        super(cause);
        this.errorCode = ErrorCode.FORBIDDEN;
    }
}

