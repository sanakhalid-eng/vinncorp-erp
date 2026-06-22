package com.vinncorp.erp.modules.workflow.controller;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowStatusOrderRequest;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowStatusRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowStatusResponse;
import com.vinncorp.erp.modules.workflow.service.WorkflowStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/projects/{projectId} /workflow/statuses") @RequiredArgsConstructor
@Tag(name = "Workflow Statuses") 
public class WorkflowStatusController {
private final WorkflowStatusService workflowStatusService;
@PostMapping @Operation(summary = "Manage statuses", description = "CRUD operations for workflow statuses") 
public ResponseEntity<ApiResponse<WorkflowStatusResponse>> create(
@PathVariable Long projectId, @RequestBody WorkflowStatusRequest request) {
return ResponseEntity.ok(new ApiResponse<>(true, "Status created", workflowStatusService.createStatus(projectId, request)));
} @GetMapping @Operation(summary = "Manage statuses", description = "CRUD operations for workflow statuses") 
public ResponseEntity<ApiResponse<List<WorkflowStatusResponse>>> getAll(
@PathVariable Long projectId) {
return ResponseEntity.ok(new ApiResponse<>(true, "Statuses fetched", workflowStatusService.getStatuses(projectId)));
} @PatchMapping("/{statusId} ") @Operation(summary = "Manage statuses", description = "CRUD operations for workflow statuses") 
public ResponseEntity<ApiResponse<WorkflowStatusResponse>> update(
@PathVariable Long projectId, @PathVariable Long statusId, @RequestBody WorkflowStatusRequest request) {
return ResponseEntity.ok(new ApiResponse<>(true, "Status updated", workflowStatusService.updateStatus(projectId, statusId, request)));
} @PatchMapping("/reorder") @Operation(summary = "Manage statuses", description = "CRUD operations for workflow statuses") 
public ResponseEntity<ApiResponse<List<WorkflowStatusResponse>>> reorder(
@PathVariable Long projectId, @RequestBody List<WorkflowStatusOrderRequest> request) {
return ResponseEntity.ok(new ApiResponse<>(true, "Statuses reordered", workflowStatusService.reorderStatuses(projectId, request)));
} @DeleteMapping("/{statusId} ") @Operation(summary = "Manage statuses", description = "CRUD operations for workflow statuses") 
public ResponseEntity<ApiResponse<Void>> delete(
@PathVariable Long statusId) {
workflowStatusService.deleteStatus(statusId);
return ResponseEntity.ok(new ApiResponse<>(true, "Deleted", null));
}} 