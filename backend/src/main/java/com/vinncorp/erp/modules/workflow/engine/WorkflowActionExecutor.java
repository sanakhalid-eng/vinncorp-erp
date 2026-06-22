package com.vinncorp.erp.modules.workflow.engine;
import com.vinncorp.erp.modules.workflow.entity.WorkflowRule;
import java.util.Map;

public interface WorkflowActionExecutor {
void execute(WorkflowRule rule, String entityType, Long entityId, Long projectId, Map<String, Object> context);
} 