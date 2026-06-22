package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.dto.request.LeaveBalanceSeedRequest;
import com.vinncorp.erp.modules.hr.dto.response.LeaveBalanceResponse;
import com.vinncorp.erp.modules.hr.service.LeaveBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/leave-balances")
@RequiredArgsConstructor
@Tag(name = "HR Leave Balances")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get leave balances for employee")
    public ResponseEntity<List<LeaveBalanceResponse>> getBalancesByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveBalanceService.getBalancesByEmployee(employeeId, year, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/employee/{employeeId}/type/{leaveTypeId}")
    @Operation(summary = "Get specific leave balance")
    public ResponseEntity<LeaveBalanceResponse> getBalance(
            @PathVariable Long employeeId,
            @PathVariable Long leaveTypeId,
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveBalanceService.getBalance(employeeId, leaveTypeId, year, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping
    @Operation(summary = "Get all leave balances for workspace")
    public ResponseEntity<List<LeaveBalanceResponse>> getBalancesByWorkspace(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveBalanceService.getBalancesByWorkspace(workspaceId, year));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping
    @Operation(summary = "Seed/update leave balance")
    public ResponseEntity<LeaveBalanceResponse> seedBalance(@Valid @RequestBody LeaveBalanceSeedRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveBalanceService.seedBalance(request, workspaceId));
    }
}
