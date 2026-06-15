package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.GanttDataResponse;
import com.vinncorp.erp.modules.projects.service.GanttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Gantt / Timeline")
public class GanttController {

    private final GanttService ganttService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/gantt")
    @Operation(summary = "Get Gantt chart data", description = "Returns tasks with date ranges, dependencies, and sprint overlays for Gantt chart rendering")
    public ResponseEntity<ApiResponse<GanttDataResponse>> getGanttData(@PathVariable Long projectId) {
        return ResponseEntity.ok(
                ApiResponse.success("Gantt data retrieved", ganttService.getGanttData(projectId))
        );
    }
}



