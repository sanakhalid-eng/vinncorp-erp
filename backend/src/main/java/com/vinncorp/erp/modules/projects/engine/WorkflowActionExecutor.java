package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.entity.WorkflowRule;

import java.util.Map;

public interface WorkflowActionExecutor {

    void execute(WorkflowRule rule, String entityType, Long entityId, Long projectId, Map<String, Object> context);
}



