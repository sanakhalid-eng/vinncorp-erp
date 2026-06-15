package com.vinncorp.erp.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_JSON("INVALID_JSON", HttpStatus.BAD_REQUEST, "Malformed JSON in request body"),
    MISSING_PARAMETER("MISSING_PARAMETER", HttpStatus.BAD_REQUEST, "Required parameter is missing"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported"),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),

    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),
    WORKSPACE_NOT_FOUND("WORKSPACE_NOT_FOUND", HttpStatus.NOT_FOUND, "Workspace not found"),
    PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND, "Project not found"),
    TASK_NOT_FOUND("TASK_NOT_FOUND", HttpStatus.NOT_FOUND, "Task not found"),
    SUBSCRIPTION_NOT_FOUND("SUBSCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND, "Subscription not found"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", HttpStatus.NOT_FOUND, "Role not found"),
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND, "Comment not found"),
    LABEL_NOT_FOUND("LABEL_NOT_FOUND", HttpStatus.NOT_FOUND, "Label not found"),
    SPRINT_NOT_FOUND("SPRINT_NOT_FOUND", HttpStatus.NOT_FOUND, "Sprint not found"),
    DEPENDENCY_NOT_FOUND("DEPENDENCY_NOT_FOUND", HttpStatus.NOT_FOUND, "Dependency not found"),

    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.BAD_REQUEST, "Invalid or expired token"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, "Token has expired"),
    MISSING_TOKEN("MISSING_TOKEN", HttpStatus.UNAUTHORIZED, "Authentication token is missing"),

    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "Access denied"),
    INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", HttpStatus.FORBIDDEN, "Insufficient permissions"),
    FORBIDDEN_OPERATION("FORBIDDEN_OPERATION", HttpStatus.FORBIDDEN, "Operation not allowed"),
    PLAN_LIMIT_EXCEEDED("PLAN_LIMIT_EXCEEDED", HttpStatus.FORBIDDEN, "Plan limit exceeded"),
    NOT_WORKSPACE_MEMBER("NOT_WORKSPACE_MEMBER", HttpStatus.FORBIDDEN, "User is not a member of this workspace"),

    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "Resource conflict"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "Email is already registered"),
    SLUG_ALREADY_EXISTS("SLUG_ALREADY_EXISTS", HttpStatus.CONFLICT, "Workspace slug already exists"),
    DUPLICATE_MEMBER("DUPLICATE_MEMBER", HttpStatus.CONFLICT, "User is already a member"),
    DUPLICATE_ROLE("DUPLICATE_ROLE", HttpStatus.CONFLICT, "Role already exists"),

    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", HttpStatus.BAD_REQUEST, "File upload failed"),
    FILE_TOO_LARGE("FILE_TOO_LARGE", HttpStatus.BAD_REQUEST, "File size exceeds limit"),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", HttpStatus.BAD_REQUEST, "Invalid file type"),
    FILE_EMPTY("FILE_EMPTY", HttpStatus.BAD_REQUEST, "File is empty"),

    SECURITY_VIOLATION("SECURITY_VIOLATION", HttpStatus.FORBIDDEN, "Security constraint violated"),
    TIMESHEET_LOCKED("TIMESHEET_LOCKED", HttpStatus.FORBIDDEN, "Timesheet is locked"),
    OWNERSHIP_REQUIRED("OWNERSHIP_REQUIRED", HttpStatus.FORBIDDEN, "Only owner can perform this action"),

    DEPENDENCY_CYCLE("DEPENDENCY_CYCLE", HttpStatus.BAD_REQUEST, "Circular dependency detected"),
    DEPENDENCY_INVALID("DEPENDENCY_INVALID", HttpStatus.BAD_REQUEST, "Invalid dependency configuration"),

    SUBSCRIPTION_INVALID("SUBSCRIPTION_INVALID", HttpStatus.BAD_REQUEST, "Invalid subscription state"),
    TRIAL_ALREADY_ACTIVE("TRIAL_ALREADY_ACTIVE", HttpStatus.CONFLICT, "Trial is already active"),
    SUBSCRIPTION_CANCELED("SUBSCRIPTION_CANCELED", HttpStatus.CONFLICT, "Subscription is already canceled");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getStatusCode() {
        return httpStatus.value();
    }
}

