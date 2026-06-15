package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.request.EscalationRuleRequest;
import com.vinncorp.erp.modules.projects.dto.response.EscalationRuleResponse;
import com.vinncorp.erp.modules.projects.entity.EscalationRule;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.EscalationRuleRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.EscalationService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscalationServiceImpl implements EscalationService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EscalationRuleRepository escalationRuleRepository;
    private final TaskRepository taskRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public EscalationRuleResponse createRule(EscalationRuleRequest request, String email) {
        EscalationRule rule = new EscalationRule();
        rule.setWorkspaceId(request.getWorkspaceId());
        rule.setProjectId(request.getProjectId());
        rule.setName(request.getName());
        rule.setTriggerCondition(request.getTriggerCondition());
        rule.setThresholdMinutes(request.getThresholdMinutes());
        rule.setEscalateToRole(request.getEscalateToRole());
        rule.setEscalateToUserId(request.getEscalateToUserId());
        rule.setNotifyAssignee(request.isNotifyAssignee());
        rule.setNotifyProjectLead(request.isNotifyProjectLead());
        rule.setAutoAssign(request.isAutoAssign());
        rule = escalationRuleRepository.save(rule);
        log.info("Escalation rule created: {}", rule.getName());
        return toResponse(rule);
    }

    @Override
    @Transactional
    public EscalationRuleResponse updateRule(Long ruleId, EscalationRuleRequest request, String email) {
        EscalationRule rule = escalationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation rule not found"));
        rule.setName(request.getName());
        rule.setTriggerCondition(request.getTriggerCondition());
        rule.setThresholdMinutes(request.getThresholdMinutes());
        rule.setEscalateToRole(request.getEscalateToRole());
        rule.setEscalateToUserId(request.getEscalateToUserId());
        rule.setNotifyAssignee(request.isNotifyAssignee());
        rule.setNotifyProjectLead(request.isNotifyProjectLead());
        rule.setAutoAssign(request.isAutoAssign());
        rule = escalationRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Override
    public List<EscalationRuleResponse> getWorkspaceRules(Long workspaceId) {
        return escalationRuleRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<EscalationRuleResponse> getProjectRules(Long workspaceId, Long projectId) {
        return escalationRuleRepository.findByWorkspaceIdAndProjectId(workspaceId, projectId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        EscalationRule rule = escalationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation rule not found"));
        escalationRuleRepository.delete(rule);
    }

    @Override
    @Transactional
    public void toggleRule(Long ruleId, boolean enabled) {
        EscalationRule rule = escalationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation rule not found"));
        rule.setEnabled(enabled);
        escalationRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public void checkAndEscalate(Task task) {
        List<EscalationRule> rules = escalationRuleRepository
                .findByProjectIdAndEnabledTrue(task.getProject().getId());

        for (EscalationRule rule : rules) {
            boolean shouldEscalate = switch (rule.getTriggerCondition()) {
                case OVERDUE -> {
                    if (task.getDueDate() == null) yield false;
                    yield ChronoUnit.DAYS.between(task.getDueDate().toLocalDate(), LocalDate.now()) >= rule.getThresholdMinutes() / 1440;
                }
                case BLOCKED_FOR -> task.getPriority() == TaskPriority.HIGH;
                case SLA_BREACH -> task.getPriority() == TaskPriority.CRITICAL;
            };

            if (shouldEscalate) {
                escalateTask(task.getId(), "Auto-escalated by rule: " + rule.getName(), "system");
            }
        }
    }

    @Override
    @Transactional
    public void escalateTask(Long taskId, String reason, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setPriority(TaskPriority.CRITICAL);
        taskRepository.save(task);

        eventPublisher.publish(DomainEvent.builder()
                .eventId("esc-" + System.currentTimeMillis())
                .type(DomainEvent.Type.TASK_UPDATED)
                .actorId(null)
                .entityType("TASK")
                .entityId(taskId)
                .projectId(task.getProject().getId())
                .message(reason)
                .build());
        log.info("Task {} escalated: {}", taskId, reason);
    }

    private EscalationRuleResponse toResponse(EscalationRule rule) {
        return EscalationRuleResponse.builder()
                .id(rule.getId())
                .workspaceId(rule.getWorkspaceId())
                .projectId(rule.getProjectId())
                .name(rule.getName())
                .triggerCondition(rule.getTriggerCondition())
                .thresholdMinutes(rule.getThresholdMinutes())
                .escalateToRole(rule.getEscalateToRole())
                .escalateToUserId(rule.getEscalateToUserId())
                .notifyAssignee(rule.isNotifyAssignee())
                .notifyProjectLead(rule.isNotifyProjectLead())
                .autoAssign(rule.isAutoAssign())
                .enabled(rule.isEnabled())
                .createdAt(rule.getCreatedAt() != null ? rule.getCreatedAt().format(DTF) : null)
                .updatedAt(rule.getUpdatedAt() != null ? rule.getUpdatedAt().format(DTF) : null)
                .build();
    }
}



