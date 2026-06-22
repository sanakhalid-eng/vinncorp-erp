package com.vinncorp.erp.modules.workflow.controller;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowRuleRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowExecutionLogResponse;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowRuleResponse;
import com.vinncorp.erp.modules.workflow.service.WorkflowRuleService;
import com.vinncorp.erp.modules.workflow.service.WorkflowTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/workflow-rules") @RequiredArgsConstructor
@Tag(name = "Workflow Rules") 
public class WorkflowRuleController {
private final WorkflowRuleService workflowRuleService;
private final WorkflowTemplateService workflowTemplateService;
@PreAuthorize("isAuthenticated()") @PostMapping @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<WorkflowRuleResponse>> createRule(
@RequestBody WorkflowRuleRequest request, @AuthenticationPrincipal UserDetails userDetails) {
return ResponseEntity.ok(new ApiResponse<>(true, "Rule created successfully", workflowRuleService.createRule(request, userDetails.getUsername())));
} @PreAuthorize("isAuthenticated()") @PutMapping("/{ruleId} ") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<WorkflowRuleResponse>> updateRule(
@PathVariable Long ruleId, @RequestBody WorkflowRuleRequest request, @AuthenticationPrincipal UserDetails userDetails) {
return ResponseEntity.ok(new ApiResponse<>(true, "Rule updated successfully", workflowRuleService.updateRule(ruleId, request, userDetails.getUsername())));
} @PreAuthorize("isAuthenticated()") @GetMapping("/{ruleId} ") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<WorkflowRuleResponse>> getRule(
@PathVariable Long ruleId) {
return ResponseEntity.ok(new ApiResponse<>(true, "Rule fetched successfully", workflowRuleService.getRule(ruleId)));
} @PreAuthorize("isAuthenticated()") @GetMapping("/workspace/{workspaceId} ") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<List<WorkflowRuleResponse>>> getWorkspaceRules(
@PathVariable Long workspaceId) {
return ResponseEntity.ok(new ApiResponse<>(true, "Rules fetched successfully", workflowRuleService.getWorkspaceRules(workspaceId)));
} @PreAuthorize("isAuthenticated()") @GetMapping("/workspace/{workspaceId} /project/{projectId} ") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<List<WorkflowRuleResponse>>> getProjectRules(
@PathVariable Long workspaceId, @PathVariable Long projectId) {
return ResponseEntity.ok(new ApiResponse<>(true, "Rules fetched successfully", workflowRuleService.getProjectRules(workspaceId, projectId)));
} @PreAuthorize("isAuthenticated()") @DeleteMapping("/{ruleId} ") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<Void>> deleteRule(
@PathVariable Long ruleId) {
workflowRuleService.deleteRule(ruleId);
return ResponseEntity.ok(new ApiResponse<>(true, "Rule deleted successfully", null));
} @PreAuthorize("isAuthenticated()") @PatchMapping("/{ruleId} /toggle") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<Void>> toggleRule(
@PathVariable Long ruleId, @RequestParam boolean enabled) {
workflowRuleService.toggleRule(ruleId, enabled);
return ResponseEntity.ok(new ApiResponse<>(true, "Rule " + (enabled ? "enabled" : "disabled") + " successfully", null));
} @PreAuthorize("isAuthenticated()") @GetMapping("/{ruleId} /logs") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<Page<WorkflowExecutionLogResponse>>> getExecutionLogs(
@PathVariable Long ruleId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
return ResponseEntity.ok(new ApiResponse<>(true, "Logs fetched successfully", workflowRuleService.getExecutionLogs(ruleId, page, size)));
} @PreAuthorize("isAuthenticated()") @GetMapping("/logs/recent") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<List<WorkflowExecutionLogResponse>>> getRecentLogs(
@RequestParam Long workspaceId) {
return ResponseEntity.ok(new ApiResponse<>(true, "Logs fetched successfully", workflowRuleService.getRecentExecutionLogs(workspaceId)));
} @PreAuthorize("isAuthenticated()") @GetMapping("/templates") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<List<WorkflowRuleResponse>>> getTemplates() {
return ResponseEntity.ok(new ApiResponse<>(true, "Templates fetched successfully", workflowTemplateService.getAvailableTemplates()));
} @PreAuthorize("isAuthenticated()") @PostMapping("/templates/{templateKey} /apply") @Operation(summary = "Manage rules", description = "CRUD operations for workflow automation rules") 
public ResponseEntity<ApiResponse<WorkflowRuleResponse>> applyTemplate(
@PathVariable String templateKey, @RequestParam Long workspaceId, @RequestParam(required = false) Long projectId, @AuthenticationPrincipal UserDetails userDetails) {
return ResponseEntity.ok(new ApiResponse<>(true, "Template applied successfully", workflowTemplateService.applyTemplate(templateKey, workspaceId, projectId, userDetails.getUsername())));
}} 