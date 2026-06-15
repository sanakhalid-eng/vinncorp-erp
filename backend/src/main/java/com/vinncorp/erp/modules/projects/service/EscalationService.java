package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.EscalationRuleRequest;
import com.vinncorp.erp.modules.projects.dto.response.EscalationRuleResponse;
import com.vinncorp.erp.modules.projects.entity.Task;

import java.util.List;

public interface EscalationService {

    EscalationRuleResponse createRule(EscalationRuleRequest request, String email);

    EscalationRuleResponse updateRule(Long ruleId, EscalationRuleRequest request, String email);

    List<EscalationRuleResponse> getWorkspaceRules(Long workspaceId);

    List<EscalationRuleResponse> getProjectRules(Long workspaceId, Long projectId);

    void deleteRule(Long ruleId);

    void toggleRule(Long ruleId, boolean enabled);

    void checkAndEscalate(Task task);

    void escalateTask(Long taskId, String reason, String email);
}



