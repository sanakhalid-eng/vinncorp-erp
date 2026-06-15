package com.vinncorp.erp.modules.projects.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vinncorp.erp.shared.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API response wrapper")
public class ApiResponse<T> {

    @Schema(example = "true", description = "Indicates if the request was successful")
    private boolean success;

    @Schema(example = "Operation completed successfully", description = "Response message")
    private String message;

    @Schema(description = "Response payload")
    private T data;

    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code")
    private int status;

    @Schema(description = "Machine-readable error code")
    private String errorCode;

    @Schema(description = "List of field-level validation errors")
    private List<FieldError> validationErrors;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.status = 200;
    }

    public ApiResponse(boolean success, String message, T data, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }

    public ApiResponse(boolean success, String message, T data, int status, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.errorCode = errorCode;
    }

    public ApiResponse(boolean success, String message, T data, int status, String errorCode, List<FieldError> validationErrors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.errorCode = errorCode;
        this.validationErrors = validationErrors;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String message, int status) {
        return new ApiResponse<>(false, message, (T) null, status);
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String message, ErrorCode errorCode) {
        return new ApiResponse<>(false, message, (T) null, errorCode.getStatusCode(), errorCode.getCode());
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String message, ErrorCode errorCode, Map<String, String> details) {
        return new ApiResponse<>(false, message, (T) details, errorCode.getStatusCode(), errorCode.getCode());
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String message, ErrorCode errorCode, List<FieldError> validationErrors) {
        return new ApiResponse<>(false, message, (T) null, errorCode.getStatusCode(), errorCode.getCode(), validationErrors);
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "Field-level validation error")
    public static class FieldError {
        @Schema(description = "Field name that failed validation")
        private String field;

        @Schema(description = "Rejected value")
        private Object rejectedValue;

        @Schema(description = "Validation error message")
        private String message;

        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }
    }
}



