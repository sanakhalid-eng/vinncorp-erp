package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidTokenException(String message) {
        super(message);
        this.errorCode = ErrorCode.INVALID_TOKEN;
    }

    public InvalidTokenException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

