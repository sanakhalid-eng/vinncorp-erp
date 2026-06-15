package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.service.ProductivityAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Productivity Analytics")
public class ProductivityAnalyticsController {

    private final ProductivityAnalyticsService productivityAnalyticsService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/productivity")
    @Operation(summary = "Get productivity", description = "Get productivity analytics for a project")
    public ResponseEntity<ApiResponse<ProductivityResponse>> getProductivity(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Productivity fetched successfully",
                productivityAnalyticsService.getProductivity(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/throughput")
    @Operation(summary = "Get throughput", description = "Get throughput analytics for a project")
    public ResponseEntity<ApiResponse<ThroughputResponse>> getThroughput(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Throughput fetched successfully",
                productivityAnalyticsService.getThroughput(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/cycle-time")
    @Operation(summary = "Get cycle time", description = "Get cycle time analytics for a project")
    public ResponseEntity<ApiResponse<CycleTimeResponse>> getCycleTime(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cycle time fetched successfully",
                productivityAnalyticsService.getCycleTime(workspaceId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/team-heatmap")
    @Operation(summary = "Get team heatmap", description = "Get team activity heatmap for a project")
    public ResponseEntity<ApiResponse<TeamHeatmapResponse>> getTeamHeatmap(
            @PathVariable Long projectId,
            @RequestParam Long workspaceId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Team heatmap fetched successfully",
                productivityAnalyticsService.getTeamHeatmap(workspaceId, projectId)));
    }
}



