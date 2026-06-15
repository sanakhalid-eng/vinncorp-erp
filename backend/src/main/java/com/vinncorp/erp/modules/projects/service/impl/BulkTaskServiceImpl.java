package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;

import com.vinncorp.erp.modules.projects.dto.request.BulkTaskUpdateRequest;
import com.vinncorp.erp.modules.projects.dto.response.BulkTaskUpdateResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowStatusRepository;
import com.vinncorp.erp.modules.projects.service.BulkTaskService;
import com.vinncorp.erp.modules.projects.service.TaskService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkTaskServiceImpl implements BulkTaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CacheService cacheService;
    private final TaskService taskService;
    private final TaskSprintRepository taskSprintRepository;
    private final SprintRepository sprintRepository;

    @Override
    @Transactional
    public BulkTaskUpdateResponse bulkUpdate(Long workspaceId, BulkTaskUpdateRequest request, String email) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkflowStatus status = null;
        if (request.getStatusId() != null) {
            status = workflowStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        TaskPriority priority = null;
        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            priority = TaskPriority.valueOf(request.getPriority().toUpperCase());
        }

        int updated = 0;
        List<Long> failed = new ArrayList<>();

        for (Long taskId : request.getTaskIds()) {
            try {
                Task task = taskRepository.findById(taskId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
                if (!workspaceId.equals(task.getProject().getWorkspace().getId())) {
                    failed.add(taskId);
                    continue;
                }
                if (status != null) task.setStatusEntity(status);
                if (assignee != null) task.setAssignee(assignee);
                if (priority != null) task.setPriority(priority);
                if (request.getSprintId() != null) {
                    Sprint sprint = sprintRepository.findById(request.getSprintId())
                            .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
                    taskSprintRepository.findByTaskId(task.getId()).ifPresent(taskSprintRepository::delete);
                    TaskSprint taskSprint = new TaskSprint();
                    taskSprint.setTask(task);
                    taskSprint.setSprint(sprint);
                    taskSprintRepository.save(taskSprint);
                }
                task.setUpdatedBy(actor.getId());
                taskRepository.save(task);
                updated++;
            } catch (Exception e) {
                failed.add(taskId);
            }
        }

        cacheService.evict(CacheNames.analytics(workspaceId));
        cacheService.evict(CacheNames.dashboard(workspaceId));

        return BulkTaskUpdateResponse.builder()
                .updatedCount(updated)
                .failedTaskIds(failed)
                .build();
    }

    @Override
    @Transactional
    public BulkTaskUpdateResponse bulkDelete(Long workspaceId, BulkTaskUpdateRequest request, String email) {
        List<Long> taskIds = request.getTaskIds();
        int updated = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Long taskId : taskIds) {
            try {
                taskService.deleteTask(taskId);
                updated++;
            } catch (Exception e) {
                failedIds.add(taskId);
                log.error("Failed to delete task {}: {}", taskId, e.getMessage());
            }
        }

        cacheService.evict(CacheNames.analytics(workspaceId));
        cacheService.evict(CacheNames.dashboard(workspaceId));

        return BulkTaskUpdateResponse.builder()
                .updatedCount(updated)
                .failedTaskIds(failedIds)
                .build();
    }
}



