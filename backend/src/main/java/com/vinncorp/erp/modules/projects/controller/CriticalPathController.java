package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.service.CriticalPathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Critical Path")
public class CriticalPathController {

    private final CriticalPathService criticalPathService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/critical-path")
    @Operation(summary = "Get critical path", description = "Get critical path analysis for a project")
    public ResponseEntity<ApiResponse<CriticalPathResponse>> getCriticalPath(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Critical path fetched successfully",
                criticalPathService.getCriticalPath(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/{taskId}/criticality")
    @Operation(summary = "Get task criticality", description = "Get criticality analysis for a task")
    public ResponseEntity<ApiResponse<CriticalTaskResponse>> getTaskCriticality(
            @PathVariable Long taskId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Task criticality fetched successfully",
                criticalPathService.getTaskCriticality(workspaceId, taskId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/delivery-risks")
    @Operation(summary = "Get delivery risks", description = "Get delivery risk analysis for a project")
    public ResponseEntity<ApiResponse<List<DeliveryRiskResponse>>> getDeliveryRisks(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery risks fetched successfully",
                criticalPathService.getDeliveryRisks(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/{taskId}/dependency-impact")
    @Operation(summary = "Get dependency impact", description = "Get dependency impact analysis for a task")
    public ResponseEntity<ApiResponse<DependencyImpactResponse>> getDependencyImpact(
            @PathVariable Long taskId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dependency impact fetched successfully",
                criticalPathService.getDependencyImpact(workspaceId, taskId)));
    }
}



