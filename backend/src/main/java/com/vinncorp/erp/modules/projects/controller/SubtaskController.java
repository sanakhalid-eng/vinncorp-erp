package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.SubtaskRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.SubtaskProgressResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class SubtaskController {

    private final TaskService taskService;

    @PostMapping("/{taskId}/subtasks")
    @Operation(summary = "Create subtask", description = "Create a new subtask under a parent task")
    public ResponseEntity<ApiResponse<TaskResponse>> createSubtask(
            @PathVariable Long taskId,
            @Valid @RequestBody SubtaskRequest request,
            Authentication authentication
    ) {
        TaskResponse subtask = taskService.createSubtask(taskId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Subtask created successfully", subtask));
    }

    @GetMapping("/{taskId}/subtasks")
    @Operation(summary = "Get subtasks", description = "Retrieve all subtasks for a task")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getSubtasks(@PathVariable Long taskId) {
        List<TaskResponse> subtasks = taskService.getSubtasks(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Subtasks fetched successfully", subtasks));
    }

    @GetMapping("/{taskId}/subtasks/progress")
    @Operation(summary = "Get subtask progress", description = "Retrieve subtask completion progress for a task")
    public ResponseEntity<ApiResponse<SubtaskProgressResponse>> getSubtaskProgress(@PathVariable Long taskId) {
        SubtaskProgressResponse progress = taskService.getSubtaskProgress(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Subtask progress fetched", progress));
    }

    @PatchMapping("/{subtaskId}")
    @Operation(summary = "Update subtask", description = "Update subtask title, description, priority, dueDate, or assignee")
    public ResponseEntity<ApiResponse<TaskResponse>> updateSubtask(
            @PathVariable Long subtaskId,
            @RequestBody SubtaskRequest request,
            Authentication authentication
    ) {
        TaskResponse updated = taskService.updateSubtask(subtaskId, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Subtask updated successfully", updated));
    }

    @PatchMapping("/{subtaskId}/toggle-completion")
    @Operation(summary = "Toggle subtask completion", description = "Toggle subtask between TODO and DONE")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleSubtaskCompletion(
            @PathVariable Long subtaskId,
            Authentication authentication
    ) {
        TaskResponse updated = taskService.toggleSubtaskCompletion(subtaskId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Subtask completion toggled", updated));
    }

    @PatchMapping("/{taskId}/parent")
    @Operation(summary = "Update subtask parent", description = "Change the parent task of a subtask")
    public ResponseEntity<ApiResponse<TaskResponse>> updateSubtaskParent(
            @PathVariable Long taskId,
            @RequestBody Map<String, Long> request,
            Authentication authentication
    ) {
        Long newParentId = request.get("parentTaskId");
        TaskResponse updated = taskService.updateSubtaskParent(taskId, newParentId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Parent task updated", updated));
    }
}



