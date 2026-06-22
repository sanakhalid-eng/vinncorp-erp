package com.vinncorp.erp.modules.workflow.dto.response;
import lombok.Data;
@Data

public class WorkflowTransitionResponse {
private Long id;
private Long fromStatusId;
private Long toStatusId;
} 