package com.vinncorp.erp.modules.projects.dto.request;

import lombok.Data;

@Data
public class WorkflowTransitionRequest {
    private Long fromStatusId;
    private Long toStatusId;
}



