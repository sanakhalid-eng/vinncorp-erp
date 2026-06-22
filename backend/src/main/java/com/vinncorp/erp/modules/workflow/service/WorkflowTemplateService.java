package com.vinncorp.erp.modules.workflow.service;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowRuleResponse;
import java.util.List;

public interface WorkflowTemplateService {
List<WorkflowRuleResponse> getAvailableTemplates();
WorkflowRuleResponse applyTemplate(String templateKey, Long workspaceId, Long projectId, String email);
} 