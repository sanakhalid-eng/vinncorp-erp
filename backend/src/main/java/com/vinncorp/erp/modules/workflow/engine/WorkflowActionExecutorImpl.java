package com.vinncorp.erp.modules.workflow.engine;

import com.vinncorp.erp.modules.projects.entity.Comment;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.CommentRepository;
import com.vinncorp.erp.modules.workflow.entity.WorkflowRule;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.workflow.repository.WorkflowStatusRepository;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowActionExecutorImpl implements WorkflowActionExecutor {
    private final TaskRepository taskRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final CommentRepository commentRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void execute(WorkflowRule rule, String entityType, Long entityId, Long projectId, Map<String, Object> context) {
        if (!"TASK".equals(entityType)) {
            log.warn("Non-task entity type {} not supported by action executor", entityType);
            return;
        }
        Task task = taskRepository.findById(entityId).orElse(null);
        if (task == null) {
            log.warn("Task {} not found for action execution", entityId);
            return;
        }
        switch (rule.getActionType()) {
            case UPDATE_STATUS -> executeUpdateStatus(task, rule, context);
            case ASSIGN_USER -> executeAssignUser(task, rule, context);
            case ADD_COMMENT -> executeAddComment(task, rule, context);
            case SET_PRIORITY -> executeSetPriority(task, rule, context);
            case SET_DUE_DATE -> executeSetDueDate(task, rule, context);
            case CREATE_SUBTASK -> executeCreateSubtask(task, rule, context);
            case SEND_NOTIFICATION -> executeSendNotification(task, rule, context);
            case MOVE_TO_SPRINT -> executeMoveToSprint(task, rule, context);
            case ADD_LABEL -> executeAddLabel(task, rule, context);
            case ESCALATE_TASK -> executeEscalateTask(task, rule, context);
            case START_SPRINT -> log.warn("START_SPRINT action not implemented for task-level execution");
        }
    }

    private void executeUpdateStatus(Task task, WorkflowRule rule, Map<String, Object> context) {
        String targetStatus = getConfigValue(rule, "targetStatus", "DONE");
        WorkflowStatus status = workflowStatusRepository.findByProjectIdAndName(task.getProject().getId(), targetStatus).orElse(null);
        if (status != null) {
            task.setStatusEntity(status);
            taskRepository.save(task);
            log.info("Auto-updated task {} status to {} ", task.getId(), targetStatus);
        }
    }

    private void executeAssignUser(Task task, WorkflowRule rule, Map<String, Object> context) {
        String assigneeEmail = getConfigValue(rule, "assigneeEmail", null);
        String assigneeRole = getConfigValue(rule, "assigneeRole", null);
        User user = null;
        if (assigneeEmail != null) {
            user = userRepository.findByEmail(assigneeEmail).orElse(null);
        } else if (assigneeRole != null && context.containsKey("projectId")) {
            Long pid = (Long) context.get("projectId");
            user = userRepository.findFirstByProjectRole(pid, assigneeRole).orElse(null);
        }
        if (user != null) {
            task.setAssignee(user);
            taskRepository.save(task);
            log.info("Auto-assigned task {} to user {} ", task.getId(), user.getEmail());
        }
    }

    private void executeAddComment(Task task, WorkflowRule rule, Map<String, Object> context) {
        String commentText = getConfigValue(rule, "commentText", "Automated comment: Rule triggered.");
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setContent(commentText);
        comment.setUser(task.getAssignee());
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
        log.info("Auto-added comment to task {} ", task.getId());
    }

    private void executeSetPriority(Task task, WorkflowRule rule, Map<String, Object> context) {
        String priorityStr = getConfigValue(rule, "priority", "MEDIUM");
        try {
            TaskPriority priority = TaskPriority.valueOf(priorityStr.toUpperCase());
            task.setPriority(priority);
            taskRepository.save(task);
            log.info("Auto-set priority for task {} to {} ", task.getId(), priority);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid priority value: {} ", priorityStr);
        }
    }

    private void executeSetDueDate(Task task, WorkflowRule rule, Map<String, Object> context) {
        String daysStr = getConfigValue(rule, "daysFromNow", "7");
        try {
            int days = Integer.parseInt(daysStr);
            task.setDueDate(LocalDateTime.now().plusDays(days));
            taskRepository.save(task);
            log.info("Auto-set due date for task {} to {} ", task.getId(), task.getDueDate());
        } catch (NumberFormatException e) {
            log.warn("Invalid daysFromNow value: {} ", daysStr);
        }
    }

    private void executeCreateSubtask(Task task, WorkflowRule rule, Map<String, Object> context) {
        String subtaskTitle = getConfigValue(rule, "subtaskTitle", "Auto-generated subtask");
        Task subtask = new Task();
        subtask.setTitle(subtaskTitle);
        subtask.setProject(task.getProject());
        subtask.setParentTask(task);
        subtask.setCreatedAt(LocalDateTime.now());
        taskRepository.save(subtask);
        log.info("Auto-created subtask for task {} : {} ", task.getId(), subtaskTitle);
    }

    private void executeSendNotification(Task task, WorkflowRule rule, Map<String, Object> context) {
        String message = getConfigValue(rule, "message", "Workflow automation triggered");
        eventPublisher.publish(DomainEvent.builder()
            .eventId("auto-" + System.currentTimeMillis())
            .type(mapTriggerToEventType(rule))
            .actorId(null)
            .entityType("TASK")
            .entityId(task.getId())
            .projectId(task.getProject().getId())
            .message(message)
            .build());
        log.info("Sent notification for task {} : {} ", task.getId(), message);
    }

    private void executeMoveToSprint(Task task, WorkflowRule rule, Map<String, Object> context) {
        String sprintIdStr = getConfigValue(rule, "sprintId", null);
        if (sprintIdStr == null && context.containsKey("sprintId")) {
            sprintIdStr = context.get("sprintId").toString();
        }
        if (sprintIdStr != null) {
            try {
                Long sprintId = Long.parseLong(sprintIdStr);
                if (sprintRepository.existsById(sprintId)) {
                    TaskSprint taskSprint = new TaskSprint();
                    taskSprint.setTask(task);
                    taskSprint.setSprint(sprintRepository.getReferenceById(sprintId));
                    taskSprint.setAssignedAt(LocalDateTime.now());
                    taskSprintRepository.save(taskSprint);
                    log.info("Moved task {} to sprint {} ", task.getId(), sprintId);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid sprintId: {} ", sprintIdStr);
            }
        }
    }

    private void executeAddLabel(Task task, WorkflowRule rule, Map<String, Object> context) {
        String labelName = getConfigValue(rule, "label", "automated");
        log.info("Label addition not implemented: would add '{}' to task {} ", labelName, task.getId());
    }

    private void executeEscalateTask(Task task, WorkflowRule rule, Map<String, Object> context) {
        task.setPriority(TaskPriority.CRITICAL);
        taskRepository.save(task);
        eventPublisher.publish(DomainEvent.builder()
            .eventId("esc-" + System.currentTimeMillis())
            .type(DomainEvent.Type.TASK_UPDATED)
            .actorId(null)
            .entityType("TASK")
            .entityId(task.getId())
            .projectId(task.getProject().getId())
            .message("Task auto-escalated: priority set to CRITICAL")
            .build());
        log.info("Escalated task {} to CRITICAL priority", task.getId());
    }

    private String getConfigValue(WorkflowRule rule, String key, String defaultValue) {
        if (rule.getActionConfig() == null || rule.getActionConfig().isBlank()) {
            return defaultValue;
        }
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(rule.getActionConfig());
            var value = node.get(key);
            return value != null ? value.asText() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private DomainEvent.Type mapTriggerToEventType(WorkflowRule rule) {
        return switch (rule.getTriggerType()) {
            case TASK_CREATED -> DomainEvent.Type.TASK_CREATED;
            case TASK_COMPLETED -> DomainEvent.Type.TASK_STATUS_CHANGED;
            case TASK_OVERDUE -> DomainEvent.Type.DUE_DATE_OVERDUE;
            case DEPENDENCY_BLOCKED -> DomainEvent.Type.TASK_DEPENDENCY_ADDED;
            default -> DomainEvent.Type.TASK_UPDATED;
        };
    }
}
