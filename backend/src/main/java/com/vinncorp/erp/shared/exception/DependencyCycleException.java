package com.vinncorp.erp.shared.exception;

import lombok.Getter;

@Getter
public class DependencyCycleException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Long sourceTaskId;
    private final Long targetTaskId;

    public DependencyCycleException(Long sourceTaskId, Long targetTaskId) {
        super("Cannot add dependency: circular dependency would be created between task " + sourceTaskId + " and task " + targetTaskId);
        this.errorCode = ErrorCode.DEPENDENCY_CYCLE;
        this.sourceTaskId = sourceTaskId;
        this.targetTaskId = targetTaskId;
    }
}

