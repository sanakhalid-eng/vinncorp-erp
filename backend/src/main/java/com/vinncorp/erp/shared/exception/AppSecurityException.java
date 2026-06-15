package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class AppSecurityException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppSecurityException(String message) {
        super(message);
        this.errorCode = ErrorCode.SECURITY_VIOLATION;
    }

    public AppSecurityException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

