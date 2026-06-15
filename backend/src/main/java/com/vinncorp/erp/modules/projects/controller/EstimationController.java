package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.EstimationAccuracyResponse;
import com.vinncorp.erp.modules.projects.dto.response.EstimationResponse;
import com.vinncorp.erp.modules.projects.dto.response.VelocityPredictionResponse;
import com.vinncorp.erp.modules.projects.service.EstimationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Estimation")
public class EstimationController {

    private final EstimationService estimationService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/{taskId}/estimate")
    @Operation(summary = "Get task estimate", description = "Get estimated effort for a task")
    public ResponseEntity<ApiResponse<EstimationResponse>> getTaskEstimate(
            @PathVariable Long taskId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Estimate fetched successfully",
                estimationService.getTaskEstimate(workspaceId, taskId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/estimation-accuracy")
    @Operation(summary = "Get project estimation accuracy", description = "Get accuracy of past estimates for a project")
    public ResponseEntity<ApiResponse<EstimationAccuracyResponse>> getProjectEstimationAccuracy(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Estimation accuracy fetched successfully",
                estimationService.getProjectEstimationAccuracy(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/velocity-prediction")
    @Operation(summary = "Get velocity prediction", description = "Get predicted velocity for a project")
    public ResponseEntity<ApiResponse<VelocityPredictionResponse>> getVelocityPrediction(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Velocity prediction fetched successfully",
                estimationService.getVelocityPrediction(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/{taskId}/similar-estimates")
    @Operation(summary = "Get similar estimates", description = "Get estimates for similar tasks")
    public ResponseEntity<ApiResponse<EstimationResponse>> getSimilarEstimates(
            @PathVariable Long taskId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Similar estimates fetched successfully",
                estimationService.getSimilarEstimates(workspaceId, taskId)));
    }
}



