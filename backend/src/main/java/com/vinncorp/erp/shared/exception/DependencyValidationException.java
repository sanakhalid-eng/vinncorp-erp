package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class DependencyValidationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String reason;

    public DependencyValidationException(String reason) {
        super(reason);
        this.errorCode = ErrorCode.DEPENDENCY_INVALID;
        this.reason = reason;
    }
}

