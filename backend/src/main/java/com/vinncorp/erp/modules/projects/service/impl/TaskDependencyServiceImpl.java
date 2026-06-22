package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.response.BlockedStatusResponse;
import com.vinncorp.erp.modules.projects.dto.response.DependencyGraphResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskDependencyResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.mapper.TaskDependencyMapper;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.DependencyValidationService;
import com.vinncorp.erp.modules.projects.service.TaskDependencyGraphService;
import com.vinncorp.erp.modules.projects.service.TaskDependencyService;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.DependencyCycleException;
import com.vinncorp.erp.shared.exception.DependencyValidationException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.shared.security.TaskPermissionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskDependencyServiceImpl implements TaskDependencyService {

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskPermissionEvaluator taskPermissionEvaluator;
    private final EventPublisher eventPublisher;
    private final ActivityLogService activityLogService;
    private final DependencyValidationService validationService;
    private final TaskDependencyGraphService graphService;
    private final CacheService cacheService;

    @Override
    @Transactional
    public TaskDependencyResponse addDependency(Long taskId, Long dependsOnTaskId, DependencyType type, String description, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        Task dependsOnTask = taskRepository.findById(dependsOnTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependency task not found"));

        if (!taskPermissionEvaluator.canUpdateTask(taskId, email)) {
            throw new BadRequestException("You are not allowed to update this task");
        }

        DependencyType resolvedType = type != null ? type : DependencyType.BLOCKED_BY;

        try {
            validationService.validateNewDependency(task, dependsOnTask, resolvedType);
        } catch (DependencyCycleException e) {
            log.warn("Cycle detected: task {} -> task {}", taskId, dependsOnTaskId);
            activityLogService.logActivity(
                    getUserIdByEmail(email),
                    EntityType.TASK,
                    taskId,
                    ActionType.DEPENDENCY_VALIDATION_FAILED,
                    null,
                    Map.of("sourceTaskId", taskId, "targetTaskId", dependsOnTaskId, "dependencyType", resolvedType.name(), "reason", "circular_dependency"),
                    "Circular dependency blocked: " + task.getTitle() + " -> " + dependsOnTask.getTitle(),
                    task.getProject().getId()
            );
            throw new BadRequestException(e.getMessage());
        } catch (DependencyValidationException e) {
            log.warn("Dependency validation failed: task {} -> task {}: {}", taskId, dependsOnTaskId, e.getMessage());
            activityLogService.logActivity(
                    getUserIdByEmail(email),
                    EntityType.TASK,
                    taskId,
                    ActionType.DEPENDENCY_VALIDATION_FAILED,
                    null,
                    Map.of(
                            "sourceTaskId", taskId,
                            "targetTaskId", dependsOnTaskId,
                            "dependencyType", resolvedType.name(),
                            "reason", resolveValidationReason(e)
                    ),
                    "Dependency validation failed: " + e.getMessage(),
                    task.getProject().getId()
            );
            throw new BadRequestException(e.getMessage());
        }

        TaskDependency dependency = new TaskDependency();
        dependency.setTask(task);
        dependency.setDependsOnTask(dependsOnTask);
        dependency.setDependencyType(resolvedType);
        dependency.setDescription(description);
        dependency.setCreatedBy(getUserIdByEmail(email));

        TaskDependency saved = taskDependencyRepository.save(dependency);

        eventPublisher.publish(DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(DomainEvent.Type.TASK_DEPENDENCY_ADDED)
                .actorId(task.getCreatedBy())
                .entityType("TASK_DEPENDENCY")
                .entityId(saved.getId())
                .projectId(task.getProject().getId())
                .message(resolvedType.name() + " dependency: " + task.getTitle() + " -> " + dependsOnTask.getTitle())
                .metadata(Map.of("taskId", taskId, "dependsOnTaskId", dependsOnTaskId, "dependencyType", resolvedType.name()))
                .build());

        activityLogService.logActivity(
                getUserIdByEmail(email),
                EntityType.TASK_DEPENDENCY,
                saved.getId(),
                ActionType.DEPENDENCY_CREATED,
                null,
                Map.of("sourceTaskId", taskId, "targetTaskId", dependsOnTaskId, "dependencyType", resolvedType.name()),
                resolvedType.name() + " dependency added: " + task.getTitle() + " -> " + dependsOnTask.getTitle(),
                task.getProject().getId()
        );

        graphService.evictCache(taskId);
        graphService.evictCache(dependsOnTaskId);

        return TaskDependencyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void removeDependency(Long taskId, Long dependsOnTaskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!taskPermissionEvaluator.canUpdateTask(taskId, email)) {
            throw new BadRequestException("You are not allowed to update this task");
        }

        TaskDependency dep = taskDependencyRepository
                .findByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId)
                .orElse(null);

        if (dep == null) {
            dep = taskDependencyRepository
                    .findByTaskIdAndDependsOnTaskId(dependsOnTaskId, taskId)
                    .orElse(null);
        }

        if (dep == null) {
            throw new ResourceNotFoundException("Dependency not found");
        }

        if (dep.isDeleted()) {
            throw new BadRequestException("Dependency already removed");
        }

        dep.softDelete(getUserIdByEmail(email));
        taskDependencyRepository.save(dep);

        eventPublisher.publish(DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(DomainEvent.Type.TASK_DEPENDENCY_REMOVED)
                .actorId(task.getCreatedBy())
                .entityType("TASK_DEPENDENCY")
                .entityId(dep.getId())
                .projectId(task.getProject().getId())
                .message("Task dependency removed")
                .metadata(Map.of("taskId", dep.getTask().getId(), "dependsOnTaskId", dep.getDependsOnTask().getId()))
                .build());

        activityLogService.logActivity(
                getUserIdByEmail(email),
                EntityType.TASK_DEPENDENCY,
                dep.getId(),
                ActionType.DEPENDENCY_REMOVED,
                null,
                null,
                "Dependency removed: " + dep.getTask().getTitle() + " -> " + dep.getDependsOnTask().getTitle(),
                task.getProject().getId()
        );

        graphService.evictCache(taskId);
        graphService.evictCache(dependsOnTaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> getDependencies(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return graphService.getDependencies(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> getBlockingTasks(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return graphService.getBlockingTasks(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> getRelatedTasks(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return graphService.getRelatedTasks(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateDependenciesBeforeStatusChange(Long taskId) {
        List<TaskDependency> dependencies = taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(taskId);
        for (TaskDependency dep : dependencies) {
            if (dep.getDependencyType() != DependencyType.BLOCKED_BY && dep.getDependencyType() != DependencyType.BLOCKS) continue;
            Task blockingTask = dep.getDependsOnTask();
            if (blockingTask == null || blockingTask.getStatusEntity() == null) continue;
            String normalized = blockingTask.getStatusEntity().getName().toUpperCase().trim();
            boolean isCompleted = normalized.equals("DONE") || normalized.equals("COMPLETED") || normalized.equals("CLOSED");
            if (!isCompleted) {
                throw new BadRequestException(
                        "Cannot proceed: task is blocked by incomplete dependency: " + blockingTask.getTitle()
                );
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BlockedStatusResponse getBlockedStatus(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return graphService.getBlockedStatus(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public DependencyGraphResponse getDependencyGraph(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return graphService.buildDependencyGraph(taskId);
    }

    private Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    @Override
    public void evictDependencyCaches(Long taskId) {
        graphService.evictCache(taskId);
    }

    @Override
    public void evictRelatedDependencyCaches(Long taskId) {
        evictDependencyCaches(taskId);
        List<TaskDependency> deps = taskDependencyRepository.findAllByTaskIdOrDependsOnTaskId(taskId);
        for (TaskDependency dep : deps) {
            if (dep.getTask() != null && !dep.getTask().getId().equals(taskId)) {
                graphService.evictCache(dep.getTask().getId());
            }
            if (dep.getDependsOnTask() != null && !dep.getDependsOnTask().getId().equals(taskId)) {
                graphService.evictCache(dep.getDependsOnTask().getId());
            }
        }
    }

    private String resolveValidationReason(DependencyValidationException e) {
        String msg = e.getMessage();
        if (msg.contains("itself")) return "SELF_DEPENDENCY";
        if (msg.contains("same project") || msg.contains("within")) return "CROSS_PROJECT_DEPENDENCY";
        if (msg.contains("already exists")) return "DUPLICATE_DEPENDENCY";
        if (msg.contains("deleted")) return "DELETED_TASK";
        return "VALIDATION_FAILED";
    }
}



