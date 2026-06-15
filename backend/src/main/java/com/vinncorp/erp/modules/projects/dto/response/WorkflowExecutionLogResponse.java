package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.ExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Workflow execution log response")
public class WorkflowExecutionLogResponse {
    private Long id;
    private Long ruleId;
    private String ruleName;
    private String entityType;
    private Long entityId;
    private ExecutionStatus status;
    private long executionTimeMs;
    private String errorMessage;
    private int retryCount;
    private String createdAt;
}



