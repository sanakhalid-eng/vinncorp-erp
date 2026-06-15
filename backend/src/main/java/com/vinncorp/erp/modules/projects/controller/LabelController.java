package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.LabelRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskLabelAssignmentRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.LabelResponse;
import com.vinncorp.erp.modules.projects.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/projects/{projectId}/labels")
    @Operation(summary = "Create label", description = "Create a new label for a project")
    public ResponseEntity<ApiResponse<LabelResponse>> createLabel(
            @PathVariable Long projectId,
            @Valid @RequestBody LabelRequest request,
            Authentication authentication
    ) {
        LabelResponse label = labelService.createLabel(projectId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Label created successfully", label));
    }

    @GetMapping("/projects/{projectId}/labels")
    @Operation(summary = "Get project labels", description = "Retrieve all labels for a project")
    public ResponseEntity<ApiResponse<List<LabelResponse>>> getLabels(@PathVariable Long projectId) {
        List<LabelResponse> labels = labelService.getLabelsByProject(projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Labels fetched successfully", labels));
    }

    @DeleteMapping("/labels/{id}")
    @Operation(summary = "Delete label", description = "Delete a label by ID")
    public ResponseEntity<ApiResponse<Void>> deleteLabel(
            @PathVariable Long id,
            Authentication authentication
    ) {
        labelService.deleteLabel(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Label deleted successfully", null));
    }

    @PostMapping("/tasks/{taskId}/labels")
    @Operation(summary = "Assign labels to task", description = "Assign labels to a task")
    public ResponseEntity<ApiResponse<List<LabelResponse>>> assignLabels(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskLabelAssignmentRequest request,
            Authentication authentication
    ) {
        List<LabelResponse> labels = labelService.assignLabelsToTask(taskId, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Labels assigned successfully", labels));
    }

    @DeleteMapping("/tasks/{taskId}/labels/{labelId}")
    @Operation(summary = "Remove label from task", description = "Remove a label from a task")
    public ResponseEntity<ApiResponse<Void>> removeLabel(
            @PathVariable Long taskId,
            @PathVariable Long labelId,
            Authentication authentication
    ) {
        labelService.removeLabelFromTask(taskId, labelId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Label removed from task", null));
    }

    @PatchMapping("/tasks/{taskId}/labels/bulk-remove")
    @Operation(summary = "Bulk remove labels from task", description = "Remove multiple labels from a task at once")
    public ResponseEntity<ApiResponse<Void>> bulkRemoveLabels(
            @PathVariable Long taskId,
            @RequestBody TaskLabelAssignmentRequest request,
            Authentication authentication
    ) {
        int removedCount = labelService.removeLabelsFromTask(taskId, request.getLabelIds(), authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, removedCount + " labels removed from task", null));
    }

    @GetMapping("/tasks/{taskId}/labels")
    @Operation(summary = "Get task labels", description = "Retrieve all labels assigned to a task")
    public ResponseEntity<ApiResponse<List<LabelResponse>>> getTaskLabels(@PathVariable Long taskId) {
        List<LabelResponse> labels = labelService.getLabelsForTask(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Task labels fetched successfully", labels));
    }
}



