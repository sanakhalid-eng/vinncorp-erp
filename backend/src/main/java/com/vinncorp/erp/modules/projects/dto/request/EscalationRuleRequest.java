package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.EscalationTrigger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Escalation rule request")
public class EscalationRuleRequest {
    private Long workspaceId;
    private Long projectId;
    private String name;
    private EscalationTrigger triggerCondition;
    private int thresholdMinutes;
    private String escalateToRole;
    private Long escalateToUserId;
    private boolean notifyAssignee = true;
    private boolean notifyProjectLead = false;
    private boolean autoAssign = false;
}



