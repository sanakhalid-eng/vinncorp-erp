package com.vinncorp.erp.modules.workflow.dto.request;
import com.vinncorp.erp.modules.workflow.engine.WorkflowTrigger;
import com.vinncorp.erp.modules.workflow.enums.WorkflowAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
@Data
@Schema(description = "Workflow rule request") 
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