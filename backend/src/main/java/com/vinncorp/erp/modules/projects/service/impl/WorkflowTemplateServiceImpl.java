package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.WorkflowRuleResponse;
import com.vinncorp.erp.modules.projects.engine.WorkflowTrigger;
import com.vinncorp.erp.modules.projects.entity.WorkflowRule;
import com.vinncorp.erp.modules.projects.enums.WorkflowAction;
import com.vinncorp.erp.modules.projects.repository.WorkflowRuleRepository;
import com.vinncorp.erp.modules.projects.service.WorkflowTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowTemplateServiceImpl implements WorkflowTemplateService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WorkflowRuleRepository ruleRepository;

    @Override
    public List<WorkflowRuleResponse> getAvailableTemplates() {
        return TEMPLATES.stream()
                .map(t -> WorkflowRuleResponse.builder()
                        .name(t.name())
                        .description(t.description())
                        .triggerType(t.trigger())
                        .actionType(t.action())
                        .conditions(Collections.emptyList())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public WorkflowRuleResponse applyTemplate(String templateKey, Long workspaceId, Long projectId, String email) {
        TemplateDef template = TEMPLATES.stream()
                .filter(t -> t.key().equals(templateKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown template: " + templateKey));

        WorkflowRule rule = new WorkflowRule();
        rule.setWorkspaceId(workspaceId);
        rule.setProjectId(projectId);
        rule.setName(template.name());
        rule.setDescription(template.description());
        rule.setEnabled(true);
        rule.setTriggerType(template.trigger());
        rule.setActionType(template.action());
        rule.setExecutionOrder(template.order());
        rule.setCreatedBy(0L);
        rule.setCreatedAt(LocalDateTime.now());

        rule = ruleRepository.save(rule);
        log.info("Applied template '{}' to workspace {}/project {}", templateKey, workspaceId, projectId);
        return toResponse(rule);
    }

    private WorkflowRuleResponse toResponse(WorkflowRule rule) {
        return WorkflowRuleResponse.builder()
                .id(rule.getId())
                .workspaceId(rule.getWorkspaceId())
                .projectId(rule.getProjectId())
                .name(rule.getName())
                .description(rule.getDescription())
                .enabled(rule.isEnabled())
                .triggerType(rule.getTriggerType())
                .actionType(rule.getActionType())
                .executionOrder(rule.getExecutionOrder())
                .createdAt(rule.getCreatedAt() != null ? rule.getCreatedAt().format(DTF) : null)
                .updatedAt(rule.getUpdatedAt() != null ? rule.getUpdatedAt().format(DTF) : null)
                .conditions(Collections.emptyList())
                .build();
    }

    private record TemplateDef(String key, String name, String description, WorkflowTrigger trigger,
                               WorkflowAction action, int order) {}

    private static final List<TemplateDef> TEMPLATES = List.of(
            new TemplateDef("auto-close-subtasks", "Auto-close completed subtasks",
                    "Automatically marks subtasks as DONE when the parent task is completed",
                    WorkflowTrigger.TASK_COMPLETED, WorkflowAction.UPDATE_STATUS, 1),
            new TemplateDef("escalate-overdue-critical", "Escalate overdue critical tasks",
                    "Escalates overdue tasks with CRITICAL priority by notifying project leads",
                    WorkflowTrigger.TASK_OVERDUE, WorkflowAction.ESCALATE_TASK, 2),
            new TemplateDef("auto-assign-by-workload", "Auto-assign by workload",
                    "Automatically assigns new tasks to the team member with the lightest workload",
                    WorkflowTrigger.TASK_CREATED, WorkflowAction.ASSIGN_USER, 3),
            new TemplateDef("notify-blocked-dependency", "Notify on blocked dependency",
                    "Sends a notification when a task becomes blocked by a dependency",
                    WorkflowTrigger.DEPENDENCY_BLOCKED, WorkflowAction.SEND_NOTIFICATION, 4),
            new TemplateDef("move-completed-to-qa", "Move completed tasks to QA",
                    "Auto-assigns completed tasks to a QA reviewer for verification",
                    WorkflowTrigger.TASK_COMPLETED, WorkflowAction.ASSIGN_USER, 5),
            new TemplateDef("alert-sprint-overload", "Alert sprint overload",
                    "Sends an alert when a sprint's total story points exceed team capacity",
                    WorkflowTrigger.SPRINT_STARTED, WorkflowAction.SEND_NOTIFICATION, 6)
    );
}



