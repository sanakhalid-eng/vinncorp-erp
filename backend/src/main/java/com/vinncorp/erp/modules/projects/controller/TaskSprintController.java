package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.service.TaskSprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class TaskSprintController {

    private final TaskSprintService taskSprintService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{taskId}/sprint/{sprintId}")
    @Operation(summary = "Assign task to sprint", description = "Assign a task to a sprint")
    public ResponseEntity<ApiResponse<Void>> assignTaskToSprint(
            @PathVariable Long taskId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        taskSprintService.assignTaskToSprint(taskId, sprintId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Task assigned to sprint successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{taskId}/sprint")
    @Operation(summary = "Remove task from sprint", description = "Remove a task from its sprint")
    public ResponseEntity<ApiResponse<Void>> removeTaskFromSprint(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        taskSprintService.removeTaskFromSprint(taskId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Task removed from sprint successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sprint/{sprintId}/tasks")
    @Operation(summary = "Get sprint tasks", description = "Retrieve all tasks assigned to a sprint")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getSprintTasks(
            @PathVariable Long sprintId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint tasks fetched successfully",
                taskSprintService.getSprintTasks(sprintId)));
    }
}



