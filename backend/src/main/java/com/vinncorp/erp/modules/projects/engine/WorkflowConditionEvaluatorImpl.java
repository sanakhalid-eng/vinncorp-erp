package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.WorkflowCondition;
import com.vinncorp.erp.modules.projects.enums.WorkflowConditionOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowConditionEvaluatorImpl implements WorkflowConditionEvaluator {

    @Override
    public boolean evaluate(List<WorkflowCondition> conditions, Task task, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        for (WorkflowCondition condition : conditions) {
            Object fieldValue = resolveFieldValue(condition.getFieldName(), task, context);
            if (!evaluateSingle(condition, fieldValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean evaluateSingle(WorkflowCondition condition, Object fieldValue) {
        String rawValue = condition.getComparisonValue();
        WorkflowConditionOperator op = condition.getOperator();

        if (fieldValue == null) {
            return op == WorkflowConditionOperator.IS_EMPTY;
        }

        String strValue = fieldValue.toString();

        return switch (op) {
            case EQUALS -> strValue.equalsIgnoreCase(rawValue);
            case NOT_EQUALS -> !strValue.equalsIgnoreCase(rawValue);
            case GREATER_THAN -> compareNumeric(strValue, rawValue) > 0;
            case LESS_THAN -> compareNumeric(strValue, rawValue) < 0;
            case CONTAINS -> strValue.toLowerCase().contains(rawValue.toLowerCase());
            case IN -> {
                Set<String> values = Arrays.stream(rawValue.split(","))
                        .map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
                yield values.contains(strValue.toLowerCase());
            }
            case IS_EMPTY -> strValue.trim().isEmpty();
            case IS_NOT_EMPTY -> !strValue.trim().isEmpty();
        };
    }

    private Object resolveFieldValue(String fieldName, Task task, Map<String, Object> context) {
        return switch (fieldName.toLowerCase()) {
            case "status" -> task.getStatusEntity() != null ? task.getStatusEntity().getName() : null;
            case "priority" -> task.getPriority() != null ? task.getPriority().name() : null;
            case "assignee" -> task.getAssignee() != null ? task.getAssignee().getId().toString() : null;
            case "duedate" -> task.getDueDate() != null ? task.getDueDate().toString() : null;
            case "tags" -> task.getTaskLabels() != null
                    ? task.getTaskLabels().stream().map(tl -> tl.getLabel().getName()).collect(Collectors.joining(","))
                    : null;
            case "sprint" -> context != null && context.containsKey("sprintId")
                    ? context.get("sprintId").toString() : null;
            case "workspacerole" -> context != null && context.containsKey("workspaceRole")
                    ? context.get("workspaceRole").toString() : null;
            default -> context != null ? context.get(fieldName) : null;
        };
    }

    private int compareNumeric(String a, String b) {
        try {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            return Double.compare(da, db);
        } catch (NumberFormatException e) {
            return a.compareToIgnoreCase(b);
        }
    }
}



