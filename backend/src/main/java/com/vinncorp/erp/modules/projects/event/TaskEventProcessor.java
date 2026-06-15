package com.vinncorp.erp.modules.projects.event;

import com.vinncorp.erp.modules.projects.entity.Label;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.projects.repository.LabelRepository;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowStatusRepository;
import com.vinncorp.erp.modules.projects.service.BurndownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskEventProcessor {

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final BurndownService burndownService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubtaskCreated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.SUBTASK_CREATED) return;

        Long parentTaskId = extractParentTaskId(event);
        if (parentTaskId != null) {
            log.debug("Event AFTER_COMMIT: Recomputing parent progress for taskId={}", parentTaskId);
            recomputeParentProgress(parentTaskId);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubtaskStatusChanged(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_STATUS_CHANGED) return;

        Long taskId = event.getEntityId();
        if (taskId == null) return;

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getParentTask() == null) return;

        log.debug("Event AFTER_COMMIT: Subtask status changed, recomputing parent progress for taskId={}", task.getParentTask().getId());
        recomputeParentProgress(task.getParentTask().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubtaskDeleted(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_DELETED) return;

        Long parentTaskId = extractParentTaskId(event);
        if (parentTaskId != null) {
            log.debug("Event AFTER_COMMIT: Subtask deleted, recomputing parent progress for taskId={}", parentTaskId);
            recomputeParentProgress(parentTaskId);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLabelAdded(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.LABEL_ADDED_TO_TASK) return;

        Long labelId = extractLabelId(event);
        if (labelId != null) {
            log.debug("Event AFTER_COMMIT: Recomputing label usage for labelId={}", labelId);
            recomputeLabelUsage(labelId);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLabelRemoved(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.LABEL_REMOVED_FROM_TASK) return;

        Long labelId = extractLabelId(event);
        if (labelId != null) {
            log.debug("Event AFTER_COMMIT: Recomputing label usage for labelId={}", labelId);
            recomputeLabelUsage(labelId);
        }
    }

    private void recomputeParentProgress(Long parentTaskId) {
        Task parent = taskRepository.findById(parentTaskId).orElse(null);
        if (parent == null) {
            log.warn("Parent task {} not found for progress recomputation", parentTaskId);
            return;
        }

        long total = taskRepository.countByParentTaskId(parentTaskId);

        Long doneStatusId = null;
        if (parent.getProject() != null) {
            doneStatusId = workflowStatusRepository.findByProjectIdAndName(parent.getProject().getId(), "DONE")
                    .map(WorkflowStatus::getId)
                    .orElse(null);
        }

        long completed = doneStatusId != null
                ? taskRepository.countByParentTaskIdAndStatusEntity_Id(parentTaskId, doneStatusId)
                : 0;

        int oldTotal = parent.getSubtaskCount();
        int oldCompleted = parent.getCompletedSubtaskCount();

        if (oldTotal != total || oldCompleted != completed) {
            parent.setSubtaskCount((int) total);
            parent.setCompletedSubtaskCount((int) completed);
            taskRepository.save(parent);
            log.info("Recomputed parent task {} progress: {}/{} (was {}/{})", parentTaskId, completed, total, oldCompleted, oldTotal);
        }
    }

    private void recomputeLabelUsage(Long labelId) {
        Label label = labelRepository.findById(labelId).orElse(null);
        if (label == null) {
            log.warn("Label {} not found for usage recomputation", labelId);
            return;
        }

        long actualUsage = labelRepository.countAllUsageByLabelId(labelId);
        int oldUsage = label.getUsageCount();

        if (oldUsage != actualUsage) {
            label.setUsageCount((int) actualUsage);
            labelRepository.save(label);
            log.info("Recomputed label {} usage: {} (was {})", labelId, actualUsage, oldUsage);
        }
    }

    private Long extractParentTaskId(DomainEvent event) {
        if (event.getMetadata() != null && event.getMetadata().containsKey("parentTaskId")) {
            Object val = event.getMetadata().get("parentTaskId");
            if (val instanceof Number) return ((Number) val).longValue();
        }
        return null;
    }

    private Long extractLabelId(DomainEvent event) {
        if (event.getMetadata() != null && event.getMetadata().containsKey("labelId")) {
            Object val = event.getMetadata().get("labelId");
            if (val instanceof Number) return ((Number) val).longValue();
        }
        return null;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDependencyAdded(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_DEPENDENCY_ADDED) return;

        Long taskId = extractTaskId(event);
        Long dependsOnTaskId = extractDependsOnTaskId(event);
        if (taskId != null && dependsOnTaskId != null) {
            log.debug("Event AFTER_COMMIT: Task dependency added: taskId={} dependsOn={}", taskId, dependsOnTaskId);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDependencyRemoved(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_DEPENDENCY_REMOVED) return;

        Long taskId = extractTaskId(event);
        Long dependsOnTaskId = extractDependsOnTaskId(event);
        if (taskId != null && dependsOnTaskId != null) {
            log.debug("Event AFTER_COMMIT: Task dependency removed: taskId={} dependsOn={}", taskId, dependsOnTaskId);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChangedForDependencies(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_STATUS_CHANGED) return;

        Long taskId = event.getEntityId();
        if (taskId == null) return;

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getStatusEntity() == null) return;

        String normalized = task.getStatusEntity().getName().toUpperCase().trim();
        boolean isDone = normalized.equals("DONE") || normalized.equals("COMPLETED") || normalized.equals("CLOSED");

        if (isDone) {
            List<TaskDependency> blockingDeps = taskDependencyRepository.findByDependsOnTaskId(taskId);
            for (TaskDependency dep : blockingDeps) {
                if (dep.getTask() != null) {
                    log.debug("Task {} completed, checking if task {} is now unblocked", taskId, dep.getTask().getId());
                }
            }
        }
    }

    private Long extractTaskId(DomainEvent event) {
        if (event.getMetadata() != null && event.getMetadata().containsKey("taskId")) {
            Object val = event.getMetadata().get("taskId");
            if (val instanceof Number) return ((Number) val).longValue();
        }
        return null;
    }

    private Long extractDependsOnTaskId(DomainEvent event) {
        if (event.getMetadata() != null && event.getMetadata().containsKey("dependsOnTaskId")) {
            Object val = event.getMetadata().get("dependsOnTaskId");
            if (val instanceof Number) return ((Number) val).longValue();
        }
        return null;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSprintCreated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.SPRINT_CREATED) return;
        log.debug("Event AFTER_COMMIT: Sprint created with id={}", event.getEntityId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSprintStarted(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.SPRINT_STARTED) return;
        log.debug("Event AFTER_COMMIT: Sprint started with id={}", event.getEntityId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSprintCompleted(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.SPRINT_COMPLETED) return;
        log.debug("Event AFTER_COMMIT: Sprint completed with id={}", event.getEntityId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAddedToSprint(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_ADDED_TO_SPRINT) return;
        Long taskId = event.getEntityId();
        Long sprintId = extractSprintId(event);
        log.debug("Event AFTER_COMMIT: Task {} added to sprint {}", taskId, sprintId);
        if (sprintId != null) {
            burndownService.computeAndSaveSnapshot(sprintId, LocalDate.now());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChangedForBurndown(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_STATUS_CHANGED) return;
        Long taskId = event.getEntityId();
        if (taskId == null) return;

        Long sprintId = extractSprintId(event);
        if (sprintId != null) {
            log.debug("Event AFTER_COMMIT: Task {} status changed, updating burndown for sprint {}", taskId, sprintId);
            burndownService.computeAndSaveSnapshot(sprintId, LocalDate.now());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskRemovedFromSprint(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_REMOVED_FROM_SPRINT) return;
        Long taskId = event.getEntityId();
        Long sprintId = extractSprintId(event);
        log.debug("Event AFTER_COMMIT: Task {} removed from sprint {}", taskId, sprintId);
        if (sprintId != null) {
            burndownService.computeAndSaveSnapshot(sprintId, LocalDate.now());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSprintStartedForBurndown(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.SPRINT_STARTED) return;
        Long sprintId = event.getEntityId();
        if (sprintId != null) {
            log.debug("Event AFTER_COMMIT: Sprint {} started, initializing burndown", sprintId);
            burndownService.computeAndSaveSnapshot(sprintId, LocalDate.now());
        }
    }

    private Long extractSprintId(DomainEvent event) {
        if (event.getMetadata() != null && event.getMetadata().containsKey("sprintId")) {
            Object val = event.getMetadata().get("sprintId");
            if (val instanceof Number) return ((Number) val).longValue();
        }
        return null;
    }
}



