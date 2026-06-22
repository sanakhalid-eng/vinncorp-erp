package com.vinncorp.erp.modules.workflow.engine;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.workflow.entity.WorkflowCondition;
import java.util.List;
import java.util.Map;

public interface WorkflowConditionEvaluator {
boolean evaluate(List<WorkflowCondition> conditions, Task task, Map<String, Object> context);
boolean evaluateSingle(WorkflowCondition condition, Object fieldValue);
} 