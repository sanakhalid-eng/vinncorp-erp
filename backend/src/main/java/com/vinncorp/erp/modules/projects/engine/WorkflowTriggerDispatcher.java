package com.vinncorp.erp.modules.projects.engine;

import java.util.Map;

public interface WorkflowTriggerDispatcher {

    void dispatch(WorkflowTrigger trigger, Long workspaceId, Long projectId, String entityType, Long entityId, Map<String, Object> context);
}



