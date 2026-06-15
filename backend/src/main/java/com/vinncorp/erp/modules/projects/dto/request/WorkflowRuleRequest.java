package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.engine.WorkflowTrigger;
import com.vinncorp.erp.modules.projects.enums.WorkflowAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create/update workflow rule request")
public class WorkflowRuleRequest {
    private Long workspaceId;
    private Long projectId;
    private String name;
    private String description;
    private WorkflowTrigger triggerType;
    private WorkflowAction actionType;
    private String actionConfig;
    private int executionOrder;
    private int cooldownSeconds;
    private List<WorkflowConditionRequest> conditions;
}



