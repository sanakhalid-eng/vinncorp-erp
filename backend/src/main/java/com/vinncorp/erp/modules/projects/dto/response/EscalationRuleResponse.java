package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.EscalationTrigger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Escalation rule response")
public class EscalationRuleResponse {
    private Long id;
    private Long workspaceId;
    private Long projectId;
    private String name;
    private EscalationTrigger triggerCondition;
    private int thresholdMinutes;
    private String escalateToRole;
    private Long escalateToUserId;
    private boolean notifyAssignee;
    private boolean notifyProjectLead;
    private boolean autoAssign;
    private boolean enabled;
    private String createdAt;
    private String updatedAt;
}


