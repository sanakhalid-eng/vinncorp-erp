package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.WorkflowStatusOrderRequest;
import com.vinncorp.erp.modules.projects.dto.request.WorkflowStatusRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowStatusResponse;
import com.vinncorp.erp.modules.projects.service.WorkflowStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/workflow/statuses")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class WorkflowStatusController {

    private final WorkflowStatusService workflowStatusService;

    @PostMapping
    @Operation(summary = "Create status", description = "Create a new workflow status for a project")
    public ResponseEntity<ApiResponse<WorkflowStatusResponse>> create(
            @PathVariable Long projectId,
            @RequestBody WorkflowStatusRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Status created",
                        workflowStatusService.createStatus(projectId, request))
        );
    }

    @GetMapping
    @Operation(summary = "Get all statuses", description = "Retrieve all workflow statuses for a project")
    public ResponseEntity<ApiResponse<List<WorkflowStatusResponse>>> getAll(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Statuses fetched",
                        workflowStatusService.getStatuses(projectId))
        );
    }

    @PatchMapping("/{statusId}")
    @Operation(summary = "Update status", description = "Update a workflow status by ID")
    public ResponseEntity<ApiResponse<WorkflowStatusResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long statusId,
            @RequestBody WorkflowStatusRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Status updated",
                        workflowStatusService.updateStatus(projectId, statusId, request))
        );
    }

    @PatchMapping("/reorder")
    @Operation(summary = "Reorder statuses", description = "Reorder workflow statuses for a project")
    public ResponseEntity<ApiResponse<List<WorkflowStatusResponse>>> reorder(
            @PathVariable Long projectId,
            @RequestBody List<WorkflowStatusOrderRequest> request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Statuses reordered",
                        workflowStatusService.reorderStatuses(projectId, request))
        );
    }

    @DeleteMapping("/{statusId}")
    @Operation(summary = "Delete status", description = "Delete a workflow status by ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long statusId) {
        workflowStatusService.deleteStatus(statusId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Deleted", null)
        );
    }
}



