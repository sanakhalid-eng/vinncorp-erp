package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.request.WorkflowConditionRequest;
import com.vinncorp.erp.modules.projects.dto.request.WorkflowRuleRequest;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowConditionResponse;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowExecutionLogResponse;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowRuleResponse;
import com.vinncorp.erp.modules.projects.entity.WorkflowCondition;
import com.vinncorp.erp.modules.projects.entity.WorkflowExecutionLog;
import com.vinncorp.erp.modules.projects.entity.WorkflowRule;
import com.vinncorp.erp.modules.projects.enums.ExecutionStatus;
import com.vinncorp.erp.modules.projects.repository.WorkflowExecutionLogRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowRuleRepository;
import com.vinncorp.erp.modules.projects.service.WorkflowRuleService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkflowRuleServiceImpl implements WorkflowRuleService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WorkflowRuleRepository ruleRepository;
    private final WorkflowExecutionLogRepository executionLogRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Override
    @Transactional
    public WorkflowRuleResponse createRule(WorkflowRuleRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkflowRule rule = new WorkflowRule();
        rule.setWorkspaceId(request.getWorkspaceId());
        rule.setProjectId(request.getProjectId());
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setEnabled(true);
        rule.setTriggerType(request.getTriggerType());
        rule.setActionType(request.getActionType());
        rule.setActionConfig(request.getActionConfig());
        rule.setExecutionOrder(request.getExecutionOrder());
        rule.setCooldownSeconds(request.getCooldownSeconds());
        rule.setCreatedBy(user.getId());

        if (request.getConditions() != null) {
            List<WorkflowCondition> conditions = new ArrayList<>();
            for (WorkflowConditionRequest cr : request.getConditions()) {
                WorkflowCondition condition = new WorkflowCondition();
                condition.setRule(rule);
                condition.setFieldName(cr.getFieldName());
                condition.setOperator(cr.getOperator());
                condition.setComparisonValue(cr.getComparisonValue());
                conditions.add(condition);
            }
            rule.setConditions(conditions);
        }

        rule = ruleRepository.save(rule);
        evictCache(request.getWorkspaceId());
        return toResponse(rule);
    }

    @Override
    @Transactional
    public WorkflowRuleResponse updateRule(Long ruleId, WorkflowRuleRequest request, String email) {
        WorkflowRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow rule not found"));

        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setTriggerType(request.getTriggerType());
        rule.setActionType(request.getActionType());
        rule.setActionConfig(request.getActionConfig());
        rule.setExecutionOrder(request.getExecutionOrder());
        rule.setCooldownSeconds(request.getCooldownSeconds());

        if (request.getProjectId() != null) {
            rule.setProjectId(request.getProjectId());
        }

        rule.getConditions().clear();
        if (request.getConditions() != null) {
            for (WorkflowConditionRequest cr : request.getConditions()) {
                WorkflowCondition condition = new WorkflowCondition();
                condition.setRule(rule);
                condition.setFieldName(cr.getFieldName());
                condition.setOperator(cr.getOperator());
                condition.setComparisonValue(cr.getComparisonValue());
                rule.getConditions().add(condition);
            }
        }

        rule = ruleRepository.save(rule);
        evictCache(rule.getWorkspaceId());
        return toResponse(rule);
    }

    @Override
    public WorkflowRuleResponse getRule(Long ruleId) {
        WorkflowRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow rule not found"));
        return toResponse(rule);
    }

    @Override
    public List<WorkflowRuleResponse> getWorkspaceRules(Long workspaceId) {
        return ruleRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowRuleResponse> getProjectRules(Long workspaceId, Long projectId) {
        return ruleRepository.findByWorkspaceIdAndProjectId(workspaceId, projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        WorkflowRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow rule not found"));
        Long workspaceId = rule.getWorkspaceId();
        ruleRepository.delete(rule);
        evictCache(workspaceId);
    }

    @Override
    @Transactional
    public void toggleRule(Long ruleId, boolean enabled) {
        WorkflowRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow rule not found"));
        rule.setEnabled(enabled);
        ruleRepository.save(rule);
        evictCache(rule.getWorkspaceId());
    }

    @Override
    @Transactional
    public void executeRule(WorkflowRule rule, String entityType, Long entityId, Long projectId) {
        long startTime = System.currentTimeMillis();
        try {
            var logEntry = new WorkflowExecutionLog();
            logEntry.setRuleId(rule.getId());
            logEntry.setEntityType(entityType);
            logEntry.setEntityId(entityId);
            logEntry.setStatus(ExecutionStatus.SUCCESS);
            logEntry.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            logEntry.setCreatedAt(LocalDateTime.now());
            executionLogRepository.save(logEntry);
        } catch (Exception e) {
            var logEntry = new WorkflowExecutionLog();
            logEntry.setRuleId(rule.getId());
            logEntry.setEntityType(entityType);
            logEntry.setEntityId(entityId);
            logEntry.setStatus(ExecutionStatus.FAILURE);
            logEntry.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            logEntry.setErrorMessage(e.getMessage());
            logEntry.setCreatedAt(LocalDateTime.now());
            executionLogRepository.save(logEntry);
        }
    }

    @Override
    public Page<WorkflowExecutionLogResponse> getExecutionLogs(Long ruleId, int page, int size) {
        return executionLogRepository.findByRuleIdOrderByCreatedAtDesc(ruleId, PageRequest.of(page, size))
                .map(this::toLogResponse);
    }

    @Override
    public List<WorkflowExecutionLogResponse> getRecentExecutionLogs(Long workspaceId) {
        List<WorkflowExecutionLog> logs = executionLogRepository
                .findByStatusOrderByCreatedAtDesc(ExecutionStatus.FAILURE);
        if (logs.isEmpty()) {
            logs = executionLogRepository
                    .findByStatusOrderByCreatedAtDesc(ExecutionStatus.SUCCESS);
        }
        return logs.stream().limit(50).map(this::toLogResponse).collect(Collectors.toList());
    }

    @Override
    public void evictCache(Long workspaceId) {
        cacheService.evict(CacheNames.recurring(workspaceId));
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
                .actionConfig(rule.getActionConfig())
                .executionOrder(rule.getExecutionOrder())
                .cooldownSeconds(rule.getCooldownSeconds())
                .lastExecutedAt(rule.getLastExecutedAt() != null ? rule.getLastExecutedAt().format(DTF) : null)
                .conditions(rule.getConditions().stream()
                        .map(c -> WorkflowConditionResponse.builder()
                                .id(c.getId())
                                .fieldName(c.getFieldName())
                                .operator(c.getOperator())
                                .comparisonValue(c.getComparisonValue())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(rule.getCreatedAt() != null ? rule.getCreatedAt().format(DTF) : null)
                .updatedAt(rule.getUpdatedAt() != null ? rule.getUpdatedAt().format(DTF) : null)
                .build();
    }

    private WorkflowExecutionLogResponse toLogResponse(WorkflowExecutionLog log) {
        String ruleName = ruleRepository.findById(log.getRuleId())
                .map(WorkflowRule::getName).orElse("Unknown");
        return WorkflowExecutionLogResponse.builder()
                .id(log.getId())
                .ruleId(log.getRuleId())
                .ruleName(ruleName)
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .status(log.getStatus())
                .executionTimeMs(log.getExecutionTimeMs())
                .errorMessage(log.getErrorMessage())
                .retryCount(log.getRetryCount())
                .createdAt(log.getCreatedAt() != null ? log.getCreatedAt().format(DTF) : null)
                .build();
    }
}



