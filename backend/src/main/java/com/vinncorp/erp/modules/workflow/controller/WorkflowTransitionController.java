package com.vinncorp.erp.modules.workflow.controller;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowTransitionRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowTransitionResponse;
import com.vinncorp.erp.modules.workflow.service.WorkflowTransitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/projects/{projectId} /workflow/transitions") @RequiredArgsConstructor
@Tag(name = "Workflow Transitions") 
public class WorkflowTransitionController {
private final WorkflowTransitionService workflowTransitionService;
@PostMapping @Operation(summary = "Manage transitions", description = "CRUD operations for workflow transitions") 
public ResponseEntity<ApiResponse<WorkflowTransitionResponse>> create(
@PathVariable Long projectId, @RequestBody WorkflowTransitionRequest request) {
return ResponseEntity.ok(new ApiResponse<>(true, "Transition created", workflowTransitionService.createTransition(projectId, request)));
} @GetMapping @Operation(summary = "Manage transitions", description = "CRUD operations for workflow transitions") 
public ResponseEntity<ApiResponse<List<WorkflowTransitionResponse>>> getAll(
@PathVariable Long projectId) {
return ResponseEntity.ok(new ApiResponse<>(true, "Transitions fetched", workflowTransitionService.getTransitions(projectId)));
} @DeleteMapping("/{transitionId} ") @Operation(summary = "Manage transitions", description = "CRUD operations for workflow transitions") 
public ResponseEntity<ApiResponse<Void>> delete(
@PathVariable Long projectId, @PathVariable Long transitionId) {
workflowTransitionService.deleteTransition(projectId, transitionId);
return ResponseEntity.ok(new ApiResponse<>(true, "Transition deleted", null));
}} 