package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.DeliveryPredictabilityResponse;
import com.vinncorp.erp.modules.projects.dto.response.ExecutiveDashboardResponse;
import com.vinncorp.erp.modules.projects.service.DeliveryPredictabilityService;
import com.vinncorp.erp.modules.projects.service.ExecutiveAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Executive Analytics")
public class ExecutiveAnalyticsController {

    private final ExecutiveAnalyticsService executiveAnalyticsService;
    private final DeliveryPredictabilityService deliveryPredictabilityService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/executive/dashboard")
    @Operation(summary = "Executive workspace dashboard")
    public ResponseEntity<ApiResponse<ExecutiveDashboardResponse>> dashboard() {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Executive dashboard fetched",
                executiveAnalyticsService.getExecutiveDashboard(wsId)));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/executive/snapshots")
    public ResponseEntity<ApiResponse<Void>> captureSnapshot() {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        executiveAnalyticsService.captureSnapshot(wsId);
        return ResponseEntity.ok(ApiResponse.success("Executive snapshot captured", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/executive/trends")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> trends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Executive trends fetched",
                executiveAnalyticsService.getTrends(wsId, PageRequest.of(page, size))));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/projects/{projectId}/delivery-predictability")
    @Operation(summary = "Delivery predictability for a project")
    public ResponseEntity<ApiResponse<DeliveryPredictabilityResponse>> deliveryPredictability(
            @PathVariable Long projectId) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Delivery predictability fetched",
                deliveryPredictabilityService.analyze(wsId, projectId)));
    }
}



