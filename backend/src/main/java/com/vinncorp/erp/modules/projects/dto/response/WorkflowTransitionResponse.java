package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

@Data
public class WorkflowTransitionResponse {
    private Long id;
    private Long fromStatusId;
    private Long toStatusId;
}



