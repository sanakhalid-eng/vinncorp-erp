package com.vinncorp.erp.modules.finance.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.finance.dto.response.FinanceDashboardResponse;
import com.vinncorp.erp.modules.finance.service.FinanceDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/dashboard")
@RequiredArgsConstructor
public class FinanceDashboardController {

    private final FinanceDashboardService financeDashboardService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceDashboardResponse> dashboard() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(financeDashboardService.getDashboard(workspaceId));
    }
}
