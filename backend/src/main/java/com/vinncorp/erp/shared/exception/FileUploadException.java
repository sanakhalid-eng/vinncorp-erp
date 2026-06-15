package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class FileUploadException extends RuntimeException {
    private final ErrorCode errorCode;

    public FileUploadException(String message) {
        super(message);
        this.errorCode = ErrorCode.FILE_UPLOAD_FAILED;
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.FILE_UPLOAD_FAILED;
    }

    public FileUploadException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public FileUploadException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

