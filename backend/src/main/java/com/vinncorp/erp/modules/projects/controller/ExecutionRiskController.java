package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.RiskOverviewResponse;
import com.vinncorp.erp.modules.projects.dto.response.RiskScoreResponse;
import com.vinncorp.erp.modules.projects.dto.response.SprintRiskAnalysisResponse;
import com.vinncorp.erp.modules.projects.service.ExecutionRiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Execution Risk")
public class ExecutionRiskController {

    private final ExecutionRiskService executionRiskService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/risk-score")
    @Operation(summary = "Get project risk score", description = "Get execution risk score for a project")
    public ResponseEntity<ApiResponse<RiskScoreResponse>> getProjectRiskScore(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Risk score fetched successfully",
                executionRiskService.getProjectRiskScore(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sprints/{sprintId}/risk-analysis")
    @Operation(summary = "Get sprint risk analysis", description = "Get execution risk analysis for a sprint")
    public ResponseEntity<ApiResponse<SprintRiskAnalysisResponse>> getSprintRiskAnalysis(
            @PathVariable Long sprintId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint risk analysis fetched successfully",
                executionRiskService.getSprintRiskAnalysis(workspaceId, sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/workspaces/{workspaceId}/risk-overview")
    @Operation(summary = "Get workspace risk overview", description = "Get risk overview for a workspace")
    public ResponseEntity<ApiResponse<RiskOverviewResponse>> getWorkspaceRiskOverview(
            @PathVariable Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Workspace risk overview fetched successfully",
                executionRiskService.getWorkspaceRiskOverview(workspaceId)));
    }
}



