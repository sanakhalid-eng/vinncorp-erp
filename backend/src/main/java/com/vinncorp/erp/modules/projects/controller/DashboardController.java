package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.DashboardSummaryResponse;
import com.vinncorp.erp.modules.projects.dto.response.EmployeeDashboardResponse;
import com.vinncorp.erp.modules.projects.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "System")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Retrieve dashboard summary for the current user")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(Authentication authentication) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard summary fetched successfully", summary)
        );
    }

    @GetMapping("/employee-summary")
    @Operation(summary = "Get employee dashboard summary", description = "Retrieve employee-specific dashboard data")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getEmployeeSummary(Authentication authentication) {
        EmployeeDashboardResponse summary = dashboardService.getEmployeeDashboard(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.success("Employee dashboard fetched successfully", summary)
        );
    }
}



