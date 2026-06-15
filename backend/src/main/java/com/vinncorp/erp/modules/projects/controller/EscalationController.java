package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.EscalationRuleRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.EscalationRuleResponse;
import com.vinncorp.erp.modules.projects.service.EscalationService;
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
@RequestMapping("/api/escalation-rules")
@RequiredArgsConstructor
@Tag(name = "Escalation Rules")
public class EscalationController {

    private final EscalationService escalationService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Create escalation rule", description = "Create a new escalation rule")
    public ResponseEntity<ApiResponse<EscalationRuleResponse>> createRule(
            @RequestBody EscalationRuleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Escalation rule created successfully",
                escalationService.createRule(request, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{ruleId}")
    @Operation(summary = "Update escalation rule", description = "Update an existing escalation rule")
    public ResponseEntity<ApiResponse<EscalationRuleResponse>> updateRule(
            @PathVariable Long ruleId,
            @RequestBody EscalationRuleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Escalation rule updated successfully",
                escalationService.updateRule(ruleId, request, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get workspace rules", description = "Get all escalation rules for a workspace")
    public ResponseEntity<ApiResponse<List<EscalationRuleResponse>>> getWorkspaceRules(
            @PathVariable Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Rules fetched successfully",
                escalationService.getWorkspaceRules(workspaceId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/workspace/{workspaceId}/project/{projectId}")
    @Operation(summary = "Get project rules", description = "Get escalation rules for a project")
    public ResponseEntity<ApiResponse<List<EscalationRuleResponse>>> getProjectRules(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Rules fetched successfully",
                escalationService.getProjectRules(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{ruleId}")
    @Operation(summary = "Delete escalation rule", description = "Delete an escalation rule")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long ruleId) {
        escalationService.deleteRule(ruleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rule deleted successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{ruleId}/toggle")
    @Operation(summary = "Toggle escalation rule", description = "Enable or disable an escalation rule")
    public ResponseEntity<ApiResponse<Void>> toggleRule(
            @PathVariable Long ruleId,
            @RequestParam boolean enabled
    ) {
        escalationService.toggleRule(ruleId, enabled);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rule " + (enabled ? "enabled" : "disabled") + " successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/escalate/{taskId}")
    @Operation(summary = "Escalate task", description = "Manually escalate a task")
    public ResponseEntity<ApiResponse<Void>> escalateTask(
            @PathVariable Long taskId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        escalationService.escalateTask(taskId, reason, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Task escalated successfully", null));
    }
}



