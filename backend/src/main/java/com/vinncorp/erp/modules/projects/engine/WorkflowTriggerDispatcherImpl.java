package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.WorkflowExecutionLog;
import com.vinncorp.erp.modules.projects.entity.WorkflowRule;
import com.vinncorp.erp.modules.projects.enums.ExecutionStatus;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowExecutionLogRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowTriggerDispatcherImpl implements WorkflowTriggerDispatcher {

    private final WorkflowRuleRepository ruleRepository;
    private final WorkflowExecutionLogRepository executionLogRepository;
    private final TaskRepository taskRepository;
    private final WorkflowConditionEvaluator conditionEvaluator;
    private final WorkflowActionExecutor actionExecutor;

    @Override
    @Transactional
    public void dispatch(WorkflowTrigger trigger, Long workspaceId, Long projectId, String entityType, Long entityId, Map<String, Object> context) {
        List<WorkflowRule> rules = ruleRepository.findActiveRulesByTriggerAndWorkspace(workspaceId, projectId, trigger);

        for (WorkflowRule rule : rules) {
            long startTime = System.currentTimeMillis();
            try {
                if (isOnCooldown(rule)) {
                    log.debug("Rule {} is on cooldown, skipping", rule.getId());
                    continue;
                }

                if ("TASK".equals(entityType) && !rule.getConditions().isEmpty()) {
                    Optional<Task> taskOpt = taskRepository.findById(entityId);
                    if (taskOpt.isEmpty()) {
                        log.warn("Task {} not found for trigger dispatch", entityId);
                        continue;
                    }
                    Task task = taskOpt.get();
                    if (!conditionEvaluator.evaluate(rule.getConditions(), task, context)) {
                        log.debug("Conditions not met for rule {} on task {}", rule.getId(), entityId);
                        logExecution(rule, entityType, entityId, ExecutionStatus.SKIPPED, 0, null);
                        continue;
                    }
                }

                actionExecutor.execute(rule, entityType, entityId, projectId, context);
                long executionTime = System.currentTimeMillis() - startTime;

                rule.setLastExecutedAt(LocalDateTime.now());
                ruleRepository.save(rule);

                logExecution(rule, entityType, entityId, ExecutionStatus.SUCCESS, executionTime, null);
                log.info("Executed rule {} ({}) for {} {}", rule.getId(), rule.getName(), entityType, entityId);

            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                log.error("Error executing rule {} for {} {}: {}", rule.getId(), entityType, entityId, e.getMessage());
                logExecution(rule, entityType, entityId, ExecutionStatus.FAILURE, executionTime, e.getMessage());
            }
        }
    }

    private boolean isOnCooldown(WorkflowRule rule) {
        if (rule.getCooldownSeconds() <= 0 || rule.getLastExecutedAt() == null) {
            return false;
        }
        return rule.getLastExecutedAt().plusSeconds(rule.getCooldownSeconds()).isAfter(LocalDateTime.now());
    }

    private void logExecution(WorkflowRule rule, String entityType, Long entityId, ExecutionStatus status, long executionTimeMs, String errorMessage) {
        WorkflowExecutionLog logEntry = new WorkflowExecutionLog();
        logEntry.setRuleId(rule.getId());
        logEntry.setEntityType(entityType);
        logEntry.setEntityId(entityId);
        logEntry.setStatus(status);
        logEntry.setExecutionTimeMs(executionTimeMs);
        logEntry.setErrorMessage(errorMessage);
        logEntry.setRetryCount(0);
        logEntry.setCreatedAt(LocalDateTime.now());
        executionLogRepository.save(logEntry);
    }
}



