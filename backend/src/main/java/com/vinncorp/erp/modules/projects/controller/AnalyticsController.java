package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.AnalyticsDashboardResponse;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{projectId}/analytics/dashboard")
    @Operation(summary = "Get analytics dashboard", description = "Retrieve analytics dashboard summary for a project")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> getDashboardSummary(
            @PathVariable Long projectId
    ) {
        AnalyticsDashboardResponse dashboard = analyticsService.getDashboardSummary(projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Analytics dashboard data fetched successfully", dashboard));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{projectId}/analytics/time")
    @Operation(summary = "Get time analytics", description = "Retrieve time analytics data for a project")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse.TimeAnalytics>> getTimeAnalytics(
            @PathVariable Long projectId
    ) {
        AnalyticsDashboardResponse.TimeAnalytics timeAnalytics = analyticsService.getTimeAnalytics(projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Time analytics data fetched successfully", timeAnalytics));
    }
}



