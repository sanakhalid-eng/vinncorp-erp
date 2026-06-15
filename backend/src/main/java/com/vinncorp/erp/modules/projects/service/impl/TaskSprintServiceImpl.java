package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.mapper.TaskMapper;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.TaskSprintService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskSprintServiceImpl implements TaskSprintService {

    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void assignTaskToSprint(Long taskId, Long sprintId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        var sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        if (!task.getProject().getId().equals(sprint.getProject().getId())) {
            throw new BadRequestException("Task and sprint must belong to the same project");
        }

        // Check if task already in a sprint
        if (taskSprintRepository.existsByTaskIdAndSprintId(taskId, sprintId)) {
            throw new BadRequestException("Task is already assigned to this sprint");
        }

        // SUBTASK CONSISTENCY: Parent and subtasks must be in SAME sprint
        if (task.getParentTask() != null) {
            // This is a subtask - check if parent is in a different sprint
            taskSprintRepository.findByTaskId(task.getParentTask().getId()).ifPresent(parentTs -> {
                if (!parentTs.getSprint().getId().equals(sprintId)) {
                    throw new BadRequestException(
                            "Cannot assign subtask to different sprint than parent. Parent is in sprint: " +
                                    parentTs.getSprint().getName()
                    );
                }
            });
        }

        // If this task has subtasks, auto-assign them to the same sprint
        if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
            for (Task subtask : task.getSubtasks()) {
                if (!taskSprintRepository.existsByTaskIdAndSprintId(subtask.getId(), sprintId)) {
                    TaskSprint subtaskSprint = new TaskSprint();
                    subtaskSprint.setTask(subtask);
                    subtaskSprint.setSprint(sprint);
                    subtaskSprint.setAssignedAt(LocalDateTime.now());
                    taskSprintRepository.save(subtaskSprint);
                }
            }
        }

        // Dependency awareness - check if dependencies are aligned with sprint
        List<TaskDependency> dependencies = taskDependencyRepository.findByTaskId(taskId);
        for (TaskDependency dep : dependencies) {
            if (dep.getDependsOnTask() == null) continue;
            taskSprintRepository.findByTaskId(dep.getDependsOnTask().getId()).ifPresent(depTs -> {
                if (!depTs.getSprint().getId().equals(sprintId)) {
                    // Cross-sprint dependency - warning only, don't block
                    // Flag is exposed via TaskStateResolver
                }
            });
        }

        TaskSprint taskSprint = new TaskSprint();
        taskSprint.setTask(task);
        taskSprint.setSprint(sprint);
        taskSprint.setAssignedAt(LocalDateTime.now());

        taskSprintRepository.save(taskSprint);

        // Notify assignee
        if (task.getAssignee() != null && !task.getAssignee().getEmail().equals(email)) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sprintId", sprintId);
            eventPublisher.publish(DomainEvent.builder()
                    .eventId(String.valueOf(sprintId))
                    .type(DomainEvent.Type.TASK_ADDED_TO_SPRINT)
                    .actorId(null)
                    .targetUserId(task.getAssignee().getId())
                    .entityType("TASK_SPRINT")
                    .entityId(taskId)
                    .projectId(sprint.getProject().getId())
                    .message("Task assigned to sprint: " + task.getTitle())
                    .metadata(metadata)
                    .build());
        }

        // Notify dependency task assignees about cross-sprint dependencies
        for (TaskDependency dep : dependencies) {
            if (dep.getDependsOnTask() != null && dep.getDependsOnTask().getAssignee() != null) {
                taskSprintRepository.findByTaskId(dep.getDependsOnTask().getId()).ifPresent(depTs -> {
                    if (!depTs.getSprint().getId().equals(sprintId)) {
                        eventPublisher.publish(DomainEvent.builder()
                                .eventId(String.valueOf(sprintId))
                                .type(DomainEvent.Type.TASK_DEPENDENCY_CROSS_SPRINT)
                                .actorId(null)
                                .targetUserId(dep.getDependsOnTask().getAssignee().getId())
                                .entityType("TASK_SPRINT")
                                .entityId(taskId)
                                .projectId(sprint.getProject().getId())
                                .message("Cross-sprint dependency: " + task.getTitle() + " depends on your task")
                                .build());
                    }
                });
            }
        }
    }

    @Override
    @Transactional
    public void removeTaskFromSprint(Long taskId, String email) {
        TaskSprint taskSprint = taskSprintRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task is not assigned to any sprint"));

        Long sprintId = taskSprint.getSprint().getId();
        taskSprintRepository.delete(taskSprint);

        // Publish event for burndown update
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sprintId", sprintId);
        eventPublisher.publish(DomainEvent.builder()
                .eventId(String.valueOf(sprintId))
                .type(DomainEvent.Type.TASK_REMOVED_FROM_SPRINT)
                .actorId(null)
                .entityType("TASK_SPRINT")
                .entityId(taskId)
                .projectId(taskSprint.getSprint().getProject().getId())
                .message("Task removed from sprint")
                .metadata(metadata)
                .build());
    }

    @Override
    public List<TaskResponse> getSprintTasks(Long sprintId) {
        if (!sprintRepository.existsById(sprintId)) {
            throw new ResourceNotFoundException("Sprint not found");
        }
        return taskSprintRepository.findBySprintIdWithTasks(sprintId).stream()
                .filter(ts -> ts.getTask() != null)
                .map(ts -> TaskMapper.toResponse(ts.getTask()))
                .collect(Collectors.toList());
    }

    @Override
    public Long getTaskSprintId(Long taskId) {
        return taskSprintRepository.findByTaskId(taskId)
                .map(ts -> ts.getSprint().getId())
                .orElse(null);
    }
}



