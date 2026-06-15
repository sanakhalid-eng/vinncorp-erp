package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.engine.WorkflowTrigger;
import com.vinncorp.erp.modules.projects.enums.WorkflowAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Workflow rule response")
public class WorkflowRuleResponse {
    private Long id;
    private Long workspaceId;
    private Long projectId;
    private String name;
    private String description;
    private boolean enabled;
    private WorkflowTrigger triggerType;
    private WorkflowAction actionType;
    private String actionConfig;
    private int executionOrder;
    private int cooldownSeconds;
    private String lastExecutedAt;
    private List<WorkflowConditionResponse> conditions;
    private String createdAt;
    private String updatedAt;
}



