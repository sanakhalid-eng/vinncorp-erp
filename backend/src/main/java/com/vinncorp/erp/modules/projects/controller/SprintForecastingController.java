package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.CapacityForecastResponse;
import com.vinncorp.erp.modules.projects.dto.response.MonteCarloForecastResponse;
import com.vinncorp.erp.modules.projects.service.CapacityForecastService;
import com.vinncorp.erp.modules.projects.service.MonteCarloForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Forecasting")
public class SprintForecastingController {

    private final MonteCarloForecastService monteCarloForecastService;
    private final CapacityForecastService capacityForecastService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/monte-carlo")
    @Operation(summary = "Monte Carlo sprint forecast")
    public ResponseEntity<ApiResponse<MonteCarloForecastResponse>> monteCarlo(@PathVariable Long sprintId) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Monte Carlo forecast fetched",
                monteCarloForecastService.forecast(wsId, sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/capacity-forecast")
    @Operation(summary = "Predictive capacity forecast for sprint")
    public ResponseEntity<ApiResponse<CapacityForecastResponse>> capacityForecast(@PathVariable Long sprintId) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Capacity forecast fetched",
                capacityForecastService.forecastForSprint(wsId, sprintId)));
    }
}



