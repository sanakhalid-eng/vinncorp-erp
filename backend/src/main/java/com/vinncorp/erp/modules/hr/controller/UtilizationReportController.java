package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.response.EmployeeUtilizationResponse;
import com.vinncorp.erp.modules.hr.response.UtilizationSummaryResponse;
import com.vinncorp.erp.modules.hr.service.UtilizationReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hr/utilization")
@RequiredArgsConstructor
@Tag(name = "HR Utilization Reports")
public class UtilizationReportController {

    private final UtilizationReportService utilizationReportService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping("/summary")
    @Operation(summary = "Get utilization summary for workspace")
    public ResponseEntity<UtilizationSummaryResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(utilizationReportService.getUtilizationSummary(workspaceId, startDate, endDate));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping("/employees")
    @Operation(summary = "Get all employees utilization")
    public ResponseEntity<List<EmployeeUtilizationResponse>> getEmployeeUtilization(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(utilizationReportService.getEmployeeUtilization(workspaceId, startDate, endDate));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping("/employees/{employeeId}")
    @Operation(summary = "Get specific employee utilization")
    public ResponseEntity<EmployeeUtilizationResponse> getEmployeeUtilizationById(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(utilizationReportService.getEmployeeUtilizationById(employeeId, workspaceId, startDate, endDate));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping("/departments/{departmentId}")
    @Operation(summary = "Get department utilization")
    public ResponseEntity<List<EmployeeUtilizationResponse>> getDepartmentUtilization(
            @PathVariable Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(utilizationReportService.getDepartmentUtilization(departmentId, workspaceId, startDate, endDate));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Get project utilization")
    public ResponseEntity<List<EmployeeUtilizationResponse>> getProjectUtilization(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(utilizationReportService.getProjectUtilization(projectId, workspaceId, startDate, endDate));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping("/export")
    @Operation(summary = "Export utilization report")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        byte[] data = utilizationReportService.exportUtilizationReport(workspaceId, startDate, endDate, format);

        String filename = "utilization-report-" + startDate + "-to-" + endDate + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
