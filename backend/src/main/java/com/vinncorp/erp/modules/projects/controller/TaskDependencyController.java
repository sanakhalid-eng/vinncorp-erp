package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.CreateDependencyRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.DependencyGraphResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskDependencyResponse;
import com.vinncorp.erp.modules.projects.service.TaskDependencyService;
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
public class TaskDependencyController {

    private final TaskDependencyService taskDependencyService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{taskId}/dependencies")
    @Operation(summary = "Add dependency", description = "Add a dependency between two tasks")
    public ResponseEntity<ApiResponse<TaskDependencyResponse>> addDependency(
            @PathVariable Long taskId,
            @RequestBody CreateDependencyRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dependency added successfully",
                taskDependencyService.addDependency(
                        taskId,
                        request.getDependsOnTaskId(),
                        request.getDependencyType(),
                        request.getDescription(),
                        userDetails.getUsername()
                )));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{taskId}/dependencies")
    @Operation(summary = "Remove dependency", description = "Remove a dependency between two tasks")
    public ResponseEntity<ApiResponse<Void>> removeDependency(
            @PathVariable Long taskId,
            @RequestParam Long dependsOnTaskId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        taskDependencyService.removeDependency(taskId, dependsOnTaskId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Dependency removed successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{taskId}/dependencies")
    @Operation(summary = "Get dependencies", description = "Retrieve all dependencies for a task")
    public ResponseEntity<ApiResponse<List<TaskDependencyResponse>>> getDependencies(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dependencies fetched successfully",
                taskDependencyService.getDependencies(taskId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{taskId}/blocking-tasks")
    @Operation(summary = "Get blocking tasks", description = "Retrieve tasks that are blocking a given task")
    public ResponseEntity<ApiResponse<List<TaskDependencyResponse>>> getBlockingTasks(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Blocking tasks fetched successfully",
                taskDependencyService.getBlockingTasks(taskId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{taskId}/dependency-graph")
    @Operation(summary = "Get dependency graph", description = "Retrieve the full dependency graph for a task")
    public ResponseEntity<ApiResponse<DependencyGraphResponse>> getDependencyGraph(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dependency graph fetched successfully",
                taskDependencyService.getDependencyGraph(taskId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{taskId}/related")
    @Operation(summary = "Get related tasks", description = "Retrieve tasks related to a given task")
    public ResponseEntity<ApiResponse<List<TaskDependencyResponse>>> getRelatedTasks(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Related tasks fetched successfully",
                taskDependencyService.getRelatedTasks(taskId)));
    }
}



