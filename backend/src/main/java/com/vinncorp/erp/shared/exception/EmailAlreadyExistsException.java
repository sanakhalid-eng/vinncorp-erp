package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {
    private final ErrorCode errorCode;

    public EmailAlreadyExistsException(String message) {
        super(message);
        this.errorCode = ErrorCode.EMAIL_ALREADY_EXISTS;
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.EMAIL_ALREADY_EXISTS;
    }
}

