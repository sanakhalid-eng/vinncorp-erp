package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.WorkflowRuleRequest;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowExecutionLogResponse;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowRuleResponse;
import com.vinncorp.erp.modules.projects.entity.WorkflowRule;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WorkflowRuleService {

    WorkflowRuleResponse createRule(WorkflowRuleRequest request, String email);

    WorkflowRuleResponse updateRule(Long ruleId, WorkflowRuleRequest request, String email);

    WorkflowRuleResponse getRule(Long ruleId);

    List<WorkflowRuleResponse> getWorkspaceRules(Long workspaceId);

    List<WorkflowRuleResponse> getProjectRules(Long workspaceId, Long projectId);

    void deleteRule(Long ruleId);

    void toggleRule(Long ruleId, boolean enabled);

    void executeRule(WorkflowRule rule, String entityType, Long entityId, Long projectId);

    Page<WorkflowExecutionLogResponse> getExecutionLogs(Long ruleId, int page, int size);

    List<WorkflowExecutionLogResponse> getRecentExecutionLogs(Long workspaceId);

    void evictCache(Long workspaceId);
}



