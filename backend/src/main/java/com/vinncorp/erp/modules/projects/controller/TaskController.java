package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.MoveTaskRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskFilterRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskRequest;
import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.engine.TaskStateResolver;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.service.TaskService;
import com.vinncorp.erp.modules.projects.specification.TaskSpecification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskStateResolver taskStateResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Get tasks", description = "Retrieve paginated tasks for the current user")
    public ResponseEntity<ApiResponse<PaginatedResponse<TaskResponse>>> getTasks(
            @RequestParam(defaultValue = "false") boolean assignedToMe,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (assignedToMe) {
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "My tasks fetched successfully",
                            taskService.getMyTasks(userDetails.getUsername(), page, limit))
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Tasks fetched successfully",
                        taskService.getMyTasks(userDetails.getUsername(), page, limit))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Create task", description = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task created successfully",
                        taskService.createTask(request, userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-tasks")
    @Operation(summary = "Get my tasks", description = "Retrieve paginated tasks assigned to the current user")
    public ResponseEntity<ApiResponse<PaginatedResponse<TaskResponse>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "My tasks fetched successfully",
                        taskService.getMyTasks(userDetails.getUsername(), page, size))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a single task by its ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task fetched successfully",
                        taskService.getTaskById(id))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project", description = "Retrieve paginated tasks for a project with filters")
    public ResponseEntity<ApiResponse<PaginatedResponse<TaskResponse>>> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Tasks fetched successfully",
                        taskService.getTasksByProject(
                                projectId, statusId, priority, search, assigneeId, start, end, pageable))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update an existing task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task updated successfully",
                        taskService.updateTask(id, request, userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Update the status of a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Long statusId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task status updated",
                        taskService.updateTaskStatus(id, statusId, userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Soft-delete a task by ID")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task deleted successfully", null)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/move")
    @Operation(summary = "Move task", description = "Move a task between board columns")
    public ResponseEntity<ApiResponse<TaskResponse>> moveTask(
            @Valid @RequestBody MoveTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task moved successfully",
                        taskService.moveTask(
                                request.getTaskId(),
                                request.getSourceColumnId(),
                                request.getTargetColumnId(),
                                request.getPosition(),
                                userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/advanced-filter")
    @Operation(summary = "Advanced task filter", description = "Filter tasks with advanced criteria and return state-based grouping")
    public ResponseEntity<ApiResponse<List<TaskStateResponse>>> getTasksAdvanced(
            @RequestParam Long projectId,
            @RequestBody(required = false) TaskFilterRequest filter
    ) {
        if (filter == null) filter = new TaskFilterRequest();
        Specification<Task> spec = TaskSpecification.build(filter, projectId);
        List<TaskStateResponse> states = taskStateResolver.resolveAllByProject(projectId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Tasks fetched successfully", states)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{taskId}/state")
    @Operation(summary = "Get task state", description = "Get the current workflow state of a task")
    public ResponseEntity<ApiResponse<TaskStateResponse>> getTaskState(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task state fetched successfully",
                        taskStateResolver.resolve(taskId))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{taskId}/blocked-status")
    @Operation(summary = "Check if task is blocked", description = "Check if a task is blocked by incomplete dependencies")
    public ResponseEntity<ApiResponse<BlockedStatusResponse>> isTaskBlocked(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Blocked status fetched successfully",
                        taskService.isTaskBlocked(taskId))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{taskId}/clone")
    @Operation(summary = "Clone task", description = "Duplicate a task with all attributes except assignee and status")
    public ResponseEntity<ApiResponse<TaskResponse>> cloneTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Task cloned successfully",
                        taskService.cloneTask(taskId, userDetails.getUsername()))
        );
    }
}



