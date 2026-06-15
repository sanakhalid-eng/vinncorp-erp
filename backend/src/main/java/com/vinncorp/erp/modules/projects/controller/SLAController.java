package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.SLARequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.SLABreachReportResponse;
import com.vinncorp.erp.modules.projects.dto.response.SLAResponse;
import com.vinncorp.erp.modules.projects.enums.SLAType;
import com.vinncorp.erp.modules.projects.service.SLAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "SLA")
public class SLAController {

    private final SLAService slaService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/sla")
    @Operation(summary = "Configure SLA", description = "Set up SLA for a task")
    public ResponseEntity<ApiResponse<SLAResponse>> configureSLA(
            @PathVariable Long taskId,
            @RequestBody SLARequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        request.setTaskId(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "SLA configured successfully",
                slaService.configureSLA(request, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/{taskId}/sla")
    @Operation(summary = "Get task SLA", description = "Get SLA details for a task")
    public ResponseEntity<ApiResponse<SLAResponse>> getTaskSLA(
            @PathVariable Long taskId,
            @RequestParam SLAType slaType
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "SLA fetched successfully",
                slaService.getTaskSLA(taskId, slaType)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/sla-report")
    @Operation(summary = "Get SLA report", description = "Get SLA breach report for a project")
    public ResponseEntity<ApiResponse<SLABreachReportResponse>> getSLAReport(@PathVariable Long projectId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "SLA report fetched successfully",
                slaService.getSLAReport(projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/slas")
    @Operation(summary = "Get project SLAs", description = "Get all SLAs for a project")
    public ResponseEntity<ApiResponse<List<SLAResponse>>> getProjectSLAs(@PathVariable Long projectId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "SLAs fetched successfully",
                slaService.getProjectSLAs(projectId)));
    }
}



