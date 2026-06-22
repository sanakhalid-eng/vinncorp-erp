package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.request.SubtaskRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskFilterRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskRequest;
import com.vinncorp.erp.modules.projects.dto.response.BlockedStatusResponse;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import com.vinncorp.erp.modules.projects.dto.response.SubtaskProgressResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.engine.WorkflowValidator;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.mapper.TaskMapper;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.TaskDependencyGraphService;
import com.vinncorp.erp.modules.projects.service.TaskDependencyService;
import com.vinncorp.erp.modules.projects.service.TaskService;
import com.vinncorp.erp.modules.projects.specification.TaskSpecification;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.mapper.PaginationMapper;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.shared.security.TaskPermissionEvaluator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskPermissionEvaluator taskPermissionEvaluator;
    private final WorkflowValidator workflowValidator;
    private final ActivityLogService activityLogService;
    private final EventPublisher eventPublisher;
    private final TaskSprintRepository taskSprintRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskDependencyGraphService taskDependencyGraphService;
    private final TaskDependencyService taskDependencyService;

    /*
            CREATE TASK
         */
    @Override
    public TaskResponse createTask(TaskRequest request, String email) {
        Project project = getProject(request.getProjectId());
        User creator = getUserByEmail(email);
        User assignee = resolveAssignee(project.getId(), request.getAssigneeId());
        WorkflowStatus status = resolveStatus(project.getId(), request.getStatusId());

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatusEntity(status);
        task.setDueDate(request.getDueDate());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setProject(project);
        task.setAssignee(assignee);

        if (request.getColumnId() != null) {
            BoardColumn column = boardColumnRepository.findById(request.getColumnId())
                    .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
            task.setColumn(column);
            task.setPosition(request.getPosition() != null ? request.getPosition() : column.getTasks().size());
        }

        Task savedTask = taskRepository.save(task);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("title", savedTask.getTitle());
        newValue.put("description", savedTask.getDescription());
        newValue.put("priority", savedTask.getPriority());
        newValue.put("status", savedTask.getStatusEntity() != null ? savedTask.getStatusEntity().getName() : null);
        if (savedTask.getAssignee() != null) {
            newValue.put("assigneeId", savedTask.getAssignee().getId());
            newValue.put("assigneeName", savedTask.getAssignee().getName());
        }

        activityLogService.logActivity(
                creator.getId(),
                EntityType.TASK,
                savedTask.getId(),
                ActionType.CREATED,
                null,
                newValue,
                "Task created: " + savedTask.getTitle(),
                project.getId()
        );

        if (assignee != null && !assignee.getId().equals(creator.getId())) {
            eventPublisher.publish(DomainEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(DomainEvent.Type.TASK_ASSIGNED)
                    .actorId(creator.getId())
                    .targetUserId(assignee.getId())
                    .entityType("TASK")
                    .entityId(savedTask.getId())
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .message("You have been assigned to task: " + truncate(savedTask.getTitle(), 50))
                    .metadata(Map.of("taskTitle", savedTask.getTitle()))
                    .build());
        }

        return TaskMapper.toResponse(savedTask);
    }

    /*
        GET ALL TASKS
     */
    public List<TaskResponse> getAllTasksForUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Long> projectIds = projectMemberRepository
                .findByUser_Id(user.getId())
                .stream()
                .map(pm -> pm.getProject().getId())
                .toList();

        return taskRepository.findByProjectIdIn(projectIds)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    /*
        GET TASK BY ID
     */
    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findByIdWithLabels(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return TaskMapper.toResponse(task);
    }

    /*
        UPDATE TASK
     */
    @Override
    public TaskResponse updateTask(Long taskId, TaskRequest request, String email) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = getUserByEmail(email);

        if (!taskPermissionEvaluator.canUpdateTask(taskId, email)) {
            throw new BadRequestException("You are not allowed to update this task");
        }

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("title", task.getTitle());
        oldValue.put("description", task.getDescription());
        oldValue.put("priority", task.getPriority());
        oldValue.put("dueDate", task.getDueDate());
        if (task.getStatusEntity() != null) {
            oldValue.put("statusId", task.getStatusEntity().getId());
            oldValue.put("status", task.getStatusEntity().getName());
        }
        if (task.getAssignee() != null) {
            oldValue.put("assigneeId", task.getAssignee().getId());
            oldValue.put("assigneeName", task.getAssignee().getName());
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getStatusId() != null) {
            if (!taskPermissionEvaluator.canUpdateStatus(taskId, user.getEmail())) {
                throw new BadRequestException("You are not allowed to update this task status");
            }
            task.setStatusEntity(resolveStatus(task.getProject().getId(), request.getStatusId()));
        }

        if (request.getAssigneeId() != null || task.getAssignee() != null) {
            task.setAssignee(resolveAssignee(task.getProject().getId(), request.getAssigneeId()));
        }

        Task savedTask = taskRepository.save(task);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("title", savedTask.getTitle());
        newValue.put("description", savedTask.getDescription());
        newValue.put("priority", savedTask.getPriority());
        newValue.put("dueDate", savedTask.getDueDate());
        if (savedTask.getStatusEntity() != null) {
            newValue.put("statusId", savedTask.getStatusEntity().getId());
            newValue.put("status", savedTask.getStatusEntity().getName());
        }
        if (savedTask.getAssignee() != null) {
            newValue.put("assigneeId", savedTask.getAssignee().getId());
            newValue.put("assigneeName", savedTask.getAssignee().getName());
        }

        detectAndLogChanges(user.getId(), savedTask, oldValue, newValue);

        return TaskMapper.toResponse(savedTask);
    }

    /*
        DELETE TASK
     */
    @Override
    @Transactional
    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("id", task.getId());
        oldValue.put("title", task.getTitle());
        oldValue.put("status", task.getStatusEntity() != null ? task.getStatusEntity().getName() : null);

        if (task.getAssignee() != null) {
            oldValue.put("assigneeName", task.getAssignee().getName());
        }

        Long parentTaskId = task.getParentTask() != null ? task.getParentTask().getId() : null;
        Long projectId = task.getProject() != null ? task.getProject().getId() : null;

        activityLogService.logActivity(
                task.getCreatedBy(),
                EntityType.TASK,
                task.getId(),
                ActionType.DELETED,
                oldValue,
                null,
                "Task deleted: " + task.getTitle(),
                projectId
        );

        if (parentTaskId != null) {
            eventPublisher.publish(DomainEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(DomainEvent.Type.TASK_DELETED)
                    .actorId(task.getCreatedBy())
                    .targetUserId(null)
                    .entityType("TASK")
                    .entityId(task.getId())
                    .projectId(projectId)
                    .projectName(task.getProject() != null ? task.getProject().getName() : null)
                    .message("Subtask deleted: " + task.getTitle())
                    .metadata(Map.of("parentTaskId", parentTaskId, "subtaskId", task.getId()))
                    .build());
        }

        softDeleteDependencies(task);

        Long deletedBy = getCurrentUserId();
        task.softDelete(deletedBy);
        taskRepository.save(task);
    }

    private void softDeleteDependencies(Task task) {
        List<TaskDependency> deps = taskDependencyRepository.findByTaskId(task.getId());
        for (TaskDependency dep : deps) {
            if (!dep.isDeleted()) {
                dep.softDelete(task.getCreatedBy());
                taskDependencyRepository.save(dep);
                taskDependencyGraphService.evictCache(dep.getDependsOnTask().getId());
            }
        }
        List<TaskDependency> blockedDeps = taskDependencyRepository.findByDependsOnTaskId(task.getId());
        for (TaskDependency dep : blockedDeps) {
            if (!dep.isDeleted()) {
                dep.softDelete(task.getCreatedBy());
                taskDependencyRepository.save(dep);
                taskDependencyGraphService.evictCache(dep.getTask().getId());
            }
        }
    }

    @Override
    public BlockedStatusResponse isTaskBlocked(Long taskId) {
        return taskDependencyGraphService.getBlockedStatus(taskId);
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String email = auth.getName();
            return userRepository.findByEmail(email).map(User::getId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /*
        GET TASKS BY PROJECT
     */
    @Override
    @Transactional
    public PaginatedResponse<TaskResponse> getTasksByProject(
            Long projectId,
            Long statusId,
            String priority,
            String search,
            Long assigneeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {

        // ensure project exists
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        TaskFilterRequest filter = new TaskFilterRequest();
        filter.setStatusId(statusId);
        filter.setPriority(priority);
        filter.setAssigneeId(assigneeId);
        Specification<Task> spec = TaskSpecification.build(filter, projectId);

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);
        return PaginationMapper.toPaginatedResponse(taskPage, TaskMapper::toResponse);
    }
    @Override
    public PaginatedResponse<TaskResponse> getTasksByProjectWithFilter(
            Long projectId,
            TaskFilterRequest filter,
            int page,
            int size
    ) {

        size = Math.min(size, 50);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Task> taskPage = taskRepository.findAll(
                TaskSpecification.build(filter, projectId),
                pageable
        );

        return PaginationMapper.toPaginatedResponse(
                taskPage,
                TaskMapper::toResponse
        );
    }

    /*
        GET TASKS BY ASSIGNEE
     */
    public PaginatedResponse<TaskResponse> getMyTasks(
            String email,
            int page,
            int size
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        size = Math.min(size, 50);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Task> taskPage =
                taskRepository.findByAssigneeId(user.getId(), pageable);

        return PaginationMapper.toPaginatedResponse(
                taskPage,
                TaskMapper::toResponse
        );
    }

    /*
        UPDATE TASK STATUS (KANBAN)
     */
    @Transactional
    @Override
    public TaskResponse updateTaskStatus(Long taskId, Long newStatusId, String email) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        WorkflowStatus from = task.getStatusEntity();
        WorkflowStatus to = resolveStatus(task.getProject().getId(), newStatusId);

        // FULL VALIDATION (ENGINE)
        workflowValidator.validate(task, from.getId(), to.getId(), email);

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("statusId", from.getId());
        oldValue.put("status", from.getName());

        task.setStatusEntity(to);
        Task savedTask = taskRepository.save(task);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("statusId", to.getId());
        newValue.put("status", to.getName());

        User user = getUserByEmail(email);
        activityLogService.logActivity(
                user.getId(),
                EntityType.TASK,
                savedTask.getId(),
                ActionType.STATUS_CHANGED,
                oldValue,
                newValue,
                "Status changed from " + from.getName() + " to " + to.getName(),
                task.getProject().getId()
        );

        if (task.getAssignee() != null && !task.getAssignee().getId().equals(user.getId())) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fromStatus", from.getName());
            metadata.put("toStatus", to.getName());

            taskSprintRepository.findByTaskId(task.getId()).ifPresent(ts ->
                    metadata.put("sprintId", ts.getSprint().getId())
            );

            eventPublisher.publish(DomainEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(DomainEvent.Type.TASK_STATUS_CHANGED)
                    .actorId(user.getId())
                    .targetUserId(task.getAssignee().getId())
                    .entityType("TASK")
                    .entityId(task.getId())
                    .projectId(task.getProject().getId())
                    .projectName(task.getProject().getName())
                    .message("Task \"" + truncate(task.getTitle(), 40) + "\" status changed to " + to.getName())
                    .metadata(metadata)
                    .build());
        }

        taskDependencyService.evictRelatedDependencyCaches(taskId);

        return TaskMapper.toResponse(savedTask);
    }

    @Override
    public PaginatedResponse<TaskResponse> getTasksByAssigneeInProject(
            Long projectId,
            Long userId,
            int page,
            int size
    ) {

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        // Limit size (IMPORTANT)
        size = Math.min(size, 50);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Task> taskPage =
                taskRepository.findByProjectIdAndAssigneeId(projectId, userId, pageable);

        return PaginationMapper.toPaginatedResponse(
                taskPage,
                TaskMapper::toResponse
        );
    }

    private Long resolveWorkspaceId() {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (workspaceId == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                workspaceId = currentWorkspaceResolver.resolveDefaultWorkspace(userDetails.getUserId())
                        .map(Workspace::getId)
                        .orElseThrow(() -> new BadRequestException("No workspace context available"));
            } else {
                throw new BadRequestException("No workspace context available");
            }
        }
        return workspaceId;
    }

    private Project getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        Long workspaceId = resolveWorkspaceId();
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the current workspace");
        }
        return project;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User resolveAssignee(Long projectId, Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, assigneeId)) {
            throw new BadRequestException("Assignee must be a member of the selected project");
        }

        return assignee;
    }

    private WorkflowStatus resolveStatus(Long projectId, Long statusId) {
        WorkflowStatus status;

        if (statusId != null) {
            status = workflowStatusRepository.findById(statusId)
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        } else {
            status = workflowStatusRepository.findByIsDefaultTrueAndProjectId(projectId)
                    .orElseGet(() -> workflowStatusRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No workflow statuses found for this project")));
        }

        Long statusProjectId = status.getProject() == null ? null : status.getProject().getId();
        if (!Objects.equals(statusProjectId, projectId)) {
            throw new BadRequestException("Status does not belong to the selected project");
        }

        return status;
    }

    private BoardColumn getColumn(Long columnId) {
        return boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
    }

    @Override
    @Transactional
    public TaskResponse moveTask(Long taskId, Long sourceColumnId, Long targetColumnId, Integer position, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!taskPermissionEvaluator.canUpdateTask(taskId, email)) {
            throw new BadRequestException("You don't have permission to move this task");
        }

        BoardColumn sourceColumn = getColumn(sourceColumnId);
        BoardColumn targetColumn = getColumn(targetColumnId);
        Long projectId = task.getProject().getId();

        if (!Objects.equals(sourceColumn.getBoard().getProject().getId(), projectId) ||
            !Objects.equals(targetColumn.getBoard().getProject().getId(), projectId)) {
            throw new BadRequestException("Columns must belong to the same project");
        }

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("columnId", sourceColumn.getId());
        oldValue.put("columnName", sourceColumn.getName());
        oldValue.put("position", task.getPosition());

        task.setColumn(targetColumn);
        task.setPosition(position);
        task = taskRepository.save(task);

        reorderTasksInColumn(targetColumn, position);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("columnId", targetColumn.getId());
        newValue.put("columnName", targetColumn.getName());
        newValue.put("position", task.getPosition());

        User user = getUserByEmail(email);
        activityLogService.logActivity(
                user.getId(),
                EntityType.TASK,
                task.getId(),
                ActionType.UPDATED,
                oldValue,
                newValue,
                "Task moved from " + sourceColumn.getName() + " to " + targetColumn.getName(),
                projectId
        );

        return TaskMapper.toResponse(task);
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private void reorderTasksInColumn(BoardColumn column, Integer newPosition) {
        List<Task> tasks = taskRepository.findByColumnIdOrderByPositionAsc(column.getId());

        for (Task t : tasks) {
            if (t.getPosition() >= newPosition) {
                t.setPosition(t.getPosition() + 1);
                taskRepository.save(t);
            }
        }
    }

    private void detectAndLogChanges(Long userId, Task task, Map<String, Object> oldValue, Map<String, Object> newValue) {
        Long projectId = task.getProject() != null ? task.getProject().getId() : null;

        if (!Objects.equals(oldValue.get("title"), newValue.get("title"))) {
            activityLogService.logActivity(
                    userId,
                    EntityType.TASK,
                    task.getId(),
                    ActionType.UPDATED,
                    Map.of("title", oldValue.get("title")),
                    Map.of("title", newValue.get("title")),
                    "Title changed",
                    projectId
            );
        }

        if (!Objects.equals(oldValue.get("description"), newValue.get("description"))) {
            activityLogService.logActivity(
                    userId,
                    EntityType.TASK,
                    task.getId(),
                    ActionType.UPDATED,
                    Map.of("description", oldValue.get("description")),
                    Map.of("description", newValue.get("description")),
                    "Description changed",
                    projectId
            );
        }

        if (!Objects.equals(oldValue.get("priority"), newValue.get("priority"))) {
            activityLogService.logActivity(
                    userId,
                    EntityType.TASK,
                    task.getId(),
                    ActionType.PRIORITY_CHANGED,
                    Map.of("priority", oldValue.get("priority")),
                    Map.of("priority", newValue.get("priority")),
                    "Priority changed from " + oldValue.get("priority") + " to " + newValue.get("priority"),
                    projectId
            );
        }

        if (!Objects.equals(oldValue.get("status"), newValue.get("status"))) {
            activityLogService.logActivity(
                    userId,
                    EntityType.TASK,
                    task.getId(),
                    ActionType.STATUS_CHANGED,
                    Map.of("status", oldValue.get("status")),
                    Map.of("status", newValue.get("status")),
                    "Status changed from " + oldValue.get("status") + " to " + newValue.get("status"),
                    projectId
            );
        }

        if (!Objects.equals(oldValue.get("assigneeId"), newValue.get("assigneeId"))) {
            if (newValue.get("assigneeId") != null) {
                activityLogService.logActivity(
                        userId,
                        EntityType.TASK,
                        task.getId(),
                        ActionType.ASSIGNED,
                        Map.of("assignee", oldValue.get("assigneeName")),
                        Map.of("assignee", newValue.get("assigneeName")),
                        "Assigned to " + newValue.get("assigneeName"),
                        projectId
                );

                eventPublisher.publish(DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .type(DomainEvent.Type.TASK_ASSIGNED)
                        .actorId(userId)
                        .targetUserId((Long) newValue.get("assigneeId"))
                        .entityType("TASK")
                        .entityId(task.getId())
                        .projectId(projectId)
                        .projectName(task.getProject() != null ? task.getProject().getName() : null)
                        .message("You have been assigned to task: " + truncate(task.getTitle(), 50))
                        .build());
            } else {
                activityLogService.logActivity(
                        userId,
                        EntityType.TASK,
                        task.getId(),
                        ActionType.UNASSIGNED,
                        Map.of("assignee", oldValue.get("assigneeName")),
                        null,
                        "Unassigned from " + oldValue.get("assigneeName"),
                        projectId
                );

                if (oldValue.get("assigneeId") != null) {
                    eventPublisher.publish(DomainEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .type(DomainEvent.Type.TASK_UNASSIGNED)
                            .actorId(userId)
                            .targetUserId((Long) oldValue.get("assigneeId"))
                            .entityType("TASK")
                            .entityId(task.getId())
                            .projectId(projectId)
                            .projectName(task.getProject() != null ? task.getProject().getName() : null)
                            .message("You have been unassigned from task: " + truncate(task.getTitle(), 50))
                            .build());
                }
            }
        }

        if (!Objects.equals(oldValue.get("dueDate"), newValue.get("dueDate"))) {
            activityLogService.logActivity(
                    userId,
                    EntityType.TASK,
                    task.getId(),
                    ActionType.DUE_DATE_CHANGED,
                    Map.of("dueDate", oldValue.get("dueDate")),
                    Map.of("dueDate", newValue.get("dueDate")),
                    "Due date changed",
                    projectId
            );
        }
    }

    @Override
    @Transactional
    public TaskResponse createSubtask(Long parentTaskId, SubtaskRequest request, String email) {
        Task parent = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent task not found"));

        User creator = getUserByEmail(email);

        if (parent.getParentTask() != null) {
            throw new BadRequestException("Cannot create a subtask under another subtask (only one level allowed)");
        }

        User assignee = resolveAssignee(parent.getProject().getId(), request.getAssigneeId());
        WorkflowStatus status = resolveStatus(parent.getProject().getId(), request.getStatusId());

        var subtask = new Task();
        subtask.setTitle(request.getTitle());
        subtask.setDescription(request.getDescription());
        subtask.setPriority(request.getPriority() != null ? request.getPriority() : parent.getPriority());
        subtask.setStatusEntity(status);
        subtask.setDueDate(request.getDueDate());
        subtask.setProject(parent.getProject());
        subtask.setAssignee(assignee);
        subtask.setParentTask(parent);

        if (parent.getColumn() != null) {
            subtask.setColumn(parent.getColumn());
            List<Task> existing = taskRepository.findByColumnIdOrderByPositionAsc(parent.getColumn().getId());
            subtask.setPosition(existing.size());
        }

        Task savedSubtask = taskRepository.save(subtask);

        activityLogService.logActivity(
                creator.getId(),
                EntityType.TASK,
                savedSubtask.getId(),
                ActionType.CREATED,
                null,
                Map.of("title", savedSubtask.getTitle(), "parentTaskId", parentTaskId, "parentTaskTitle", parent.getTitle()),
                "Subtask created under: " + parent.getTitle(),
                parent.getProject().getId()
        );

        eventPublisher.publish(DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(DomainEvent.Type.SUBTASK_CREATED)
                .actorId(creator.getId())
                .targetUserId(parent.getAssignee() != null ? parent.getAssignee().getId() : parent.getCreator().getId())
                .entityType("TASK")
                .entityId(savedSubtask.getId())
                .projectId(parent.getProject().getId())
                .projectName(parent.getProject().getName())
                .message("New subtask created under \"" + truncate(parent.getTitle(), 40) + "\": " + truncate(savedSubtask.getTitle(), 40))
                .metadata(Map.of("parentTaskId", parentTaskId, "parentTaskTitle", parent.getTitle(), "subtaskId", savedSubtask.getId()))
                .build());

        return TaskMapper.toResponse(savedSubtask);
    }

    @Override
    @Transactional
    public TaskResponse toggleSubtaskCompletion(Long subtaskId, String email) {
        Task subtask = taskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        getUserByEmail(email);

        List<WorkflowStatus> statuses = workflowStatusRepository
                .findByProjectIdOrderByOrderIndexAsc(subtask.getProject().getId());

        if (statuses.isEmpty()) return TaskMapper.toResponse(subtask);

        WorkflowStatus currentStatus = subtask.getStatusEntity();
        WorkflowStatus targetStatus;

        if (currentStatus != null && isCompletionStatus(currentStatus)) {
            targetStatus = statuses.get(0);
        } else {
            targetStatus = statuses.get(statuses.size() - 1);
        }

        subtask.setStatusEntity(targetStatus);
        Task saved = taskRepository.save(subtask);
        return TaskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateSubtask(Long subtaskId, SubtaskRequest request, String email) {
        Task subtask = taskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        getUserByEmail(email);

        if (!taskPermissionEvaluator.canUpdateTask(subtaskId, email)) {
            throw new BadRequestException("You are not allowed to update this subtask");
        }

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("title", subtask.getTitle());

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            subtask.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            subtask.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            subtask.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            subtask.setDueDate(request.getDueDate());
        }
        if (request.getAssigneeId() != null) {
            subtask.setAssignee(resolveAssignee(subtask.getProject().getId(), request.getAssigneeId()));
        }

        Task saved = taskRepository.save(subtask);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("title", saved.getTitle());

        activityLogService.logActivity(
                getUserByEmail(email).getId(),
                EntityType.TASK,
                saved.getId(),
                ActionType.UPDATED,
                oldValue,
                newValue,
                "Subtask updated",
                saved.getProject().getId()
        );

        return TaskMapper.toResponse(saved);
    }

    private boolean isCompletionStatus(WorkflowStatus status) {
        String name = status.getName().toUpperCase();
        return name.contains("DONE") || name.contains("COMPLETE") || name.contains("CLOSED");
    }

    @Override
    public List<TaskResponse> getSubtasks(Long parentTaskId) {
        if (!taskRepository.existsById(parentTaskId)) {
            throw new ResourceNotFoundException("Parent task not found");
        }

        return taskRepository.findSubtasksWithLabelsByParentId(parentTaskId)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Override
    public SubtaskProgressResponse getSubtaskProgress(Long parentTaskId) {
        Task parent = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent task not found"));

        long total = taskRepository.countByParentTaskId(parentTaskId);
        Long completedStatusId = parent.getProject() != null
                ? workflowStatusRepository.findByIsDefaultTrueAndProjectId(parent.getProject().getId())
                        .map(WorkflowStatus::getId)
                        .orElse(null)
                : null;

        long completed = completedStatusId != null
                ? taskRepository.countByParentTaskIdAndStatusEntity_Id(parentTaskId, completedStatusId)
                : 0;

        long pending = total - completed;
        double percentage = total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0;

        List<TaskResponse> subtasks = taskRepository.findByParentTaskIdOrderByCreatedAtAsc(parentTaskId)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();

        return new SubtaskProgressResponse(parentTaskId, (int) total, (int) completed, (int) pending, percentage, subtasks);
    }

    @Override
    @Transactional
    public TaskResponse updateSubtaskParent(Long taskId, Long newParentTaskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = getUserByEmail(email);

        if (!taskPermissionEvaluator.canUpdateTask(taskId, email)) {
            throw new BadRequestException("You are not allowed to update this task");
        }

        Map<String, Object> oldValue = new HashMap<>();
        if (task.getParentTask() != null) {
            oldValue.put("parentTaskId", task.getParentTask().getId());
            oldValue.put("parentTaskTitle", task.getParentTask().getTitle());
        }

        if (newParentTaskId == null) {
            task.setParentTask(null);
        } else {
            Task newParent = taskRepository.findById(newParentTaskId)
                    .orElseThrow(() -> new ResourceNotFoundException("New parent task not found"));

            if (!Objects.equals(newParent.getProject().getId(), task.getProject().getId())) {
                throw new BadRequestException("Subtask must belong to the same project as parent");
            }

            if (newParent.getParentTask() != null) {
                throw new BadRequestException("Cannot assign subtask to another subtask (only one level allowed)");
            }

            if (newParent.getId().equals(taskId)) {
                throw new BadRequestException("A task cannot be its own parent");
            }

            task.setParentTask(newParent);
        }

        Task updated = taskRepository.save(task);

        Map<String, Object> newValue = new HashMap<>();
        if (updated.getParentTask() != null) {
            newValue.put("parentTaskId", updated.getParentTask().getId());
            newValue.put("parentTaskTitle", updated.getParentTask().getTitle());
        }

        if (!Objects.equals(oldValue.get("parentTaskId"), newValue.get("parentTaskId"))) {
            activityLogService.logActivity(
                    user.getId(),
                    EntityType.TASK,
                    updated.getId(),
                    ActionType.UPDATED,
                    oldValue,
                    newValue,
                    newValue.containsKey("parentTaskTitle")
                            ? "Assigned as subtask of: " + newValue.get("parentTaskTitle")
                            : "Detached from parent task",
                    updated.getProject().getId()
            );

            if (newValue.containsKey("parentTaskId")) {
                eventPublisher.publish(DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .type(DomainEvent.Type.SUBTASK_UPDATED)
                        .actorId(user.getId())
                        .targetUserId(updated.getAssignee() != null ? updated.getAssignee().getId() : user.getId())
                        .entityType("TASK")
                        .entityId(updated.getId())
                        .projectId(updated.getProject().getId())
                        .projectName(updated.getProject().getName())
                        .message("Task \"" + truncate(updated.getTitle(), 40) + "\" assigned as subtask of \"" + newValue.get("parentTaskTitle") + "\"")
                        .metadata(Map.of("parentTaskId", newValue.get("parentTaskId")))
                        .build());
            }
        }

        return TaskMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TaskResponse cloneTask(Long taskId, String email) {
        Task source = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = getUserByEmail(email);

        if (!taskPermissionEvaluator.canViewTask(taskId, email)) {
            throw new BadRequestException("You are not allowed to view this task");
        }

        Task clone = new Task();
        clone.setTitle(source.getTitle() + " (Copy)");
        clone.setDescription(source.getDescription());
        clone.setPriority(source.getPriority());
        clone.setProject(source.getProject());
        clone.setColumn(source.getColumn());
        clone.setPosition(source.getPosition());
        clone.setDueDate(source.getDueDate());
        clone.setStartDate(source.getStartDate());
        clone.setEndDate(source.getEndDate());
        clone.setStoryPoints(source.getStoryPoints());
        clone.setParentTask(null);
        clone.setSubtaskCount(0);
        clone.setCompletedSubtaskCount(0);
        clone.setReminderSent(false);

        if (source.getTaskLabels() != null) {
            List<TaskLabel> newLabels = new ArrayList<>();
            for (TaskLabel tl : source.getTaskLabels()) {
                if (tl.getLabel() != null) {
                    TaskLabel newTl = new TaskLabel();
                    newTl.setTask(clone);
                    newTl.setLabel(tl.getLabel());
                    newLabels.add(newTl);
                }
            }
            clone.setTaskLabels(newLabels);
        }

        Task savedClone = taskRepository.save(clone);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("title", savedClone.getTitle());
        newValue.put("sourceTaskId", source.getId());
        newValue.put("sourceTaskTitle", source.getTitle());
        activityLogService.logActivity(
                user.getId(),
                EntityType.TASK,
                savedClone.getId(),
                ActionType.CREATED,
                null,
                newValue,
                "Task cloned from: " + source.getTitle(),
                source.getProject().getId()
        );

        return TaskMapper.toResponse(savedClone);
    }
}



