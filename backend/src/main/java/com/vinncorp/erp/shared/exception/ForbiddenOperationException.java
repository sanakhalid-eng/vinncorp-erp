package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class ForbiddenOperationException extends RuntimeException {
    private final ErrorCode errorCode;

    public ForbiddenOperationException(String message) {
        super(message);
        this.errorCode = ErrorCode.FORBIDDEN_OPERATION;
    }

    public ForbiddenOperationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

