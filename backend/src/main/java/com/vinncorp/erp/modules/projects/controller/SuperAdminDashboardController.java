package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.SuperAdminDashboardResponse;
import com.vinncorp.erp.modules.projects.service.SuperAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService superAdminDashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get super admin dashboard summary",
               description = "Returns platform-wide statistics for super administrators")
    public ResponseEntity<ApiResponse<SuperAdminDashboardResponse>> getSummary() {
        SuperAdminDashboardResponse summary = superAdminDashboardService.getSuperAdminDashboard();
        return ResponseEntity.ok(
                ApiResponse.success("Super admin dashboard fetched successfully", summary)
        );
    }
}
