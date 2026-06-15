package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.SprintPlanRequest;
import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.service.SprintPlanningService;
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
@Tag(name = "Sprint Planning")
public class SprintPlanningController {

    private final SprintPlanningService sprintPlanningService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/sprints/plan")
    @Operation(summary = "Plan sprint", description = "Plan sprint composition based on capacity and priorities")
    public ResponseEntity<ApiResponse<SprintPlanResponse>> planSprint(
            @RequestParam Long workspaceId,
            @RequestParam Long projectId,
            @RequestBody SprintPlanRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint planned successfully",
                sprintPlanningService.planSprint(workspaceId, projectId, request)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/sprint-recommendations")
    @Operation(summary = "Get sprint recommendations", description = "Get recommendations for sprint planning")
    public ResponseEntity<ApiResponse<List<SprintRecommendation>>> getSprintRecommendations(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint recommendations fetched successfully",
                sprintPlanningService.getSprintRecommendations(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sprints/{sprintId}/capacity-risks")
    @Operation(summary = "Get capacity risks", description = "Get capacity risk analysis for a sprint")
    public ResponseEntity<ApiResponse<CapacityRiskResponse>> getCapacityRisks(
            @PathVariable Long sprintId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Capacity risks fetched successfully",
                sprintPlanningService.getCapacityRisks(workspaceId, sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/spillover-prediction")
    @Operation(summary = "Get spillover prediction", description = "Get predicted spillover for a project")
    public ResponseEntity<ApiResponse<SpilloverPredictionResponse>> getSpilloverPrediction(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Spillover prediction fetched successfully",
                sprintPlanningService.getSpilloverPrediction(workspaceId, projectId)));
    }
}



