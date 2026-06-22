package com.vinncorp.erp.modules.workflow.dto.request;
import lombok.Data;
@Data

public class WorkflowTransitionRequest {
private Long fromStatusId;
private Long toStatusId;
} 