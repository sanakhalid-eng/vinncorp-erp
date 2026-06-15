package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.VelocityTrendResponse;
import com.vinncorp.erp.modules.projects.service.VelocityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Velocity")
public class VelocityController {

    private final VelocityService velocityService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{projectId}/velocity-history")
    @Operation(summary = "Get velocity history", description = "Get velocity trend for a project")
    public ResponseEntity<ApiResponse<VelocityTrendResponse>> getVelocityHistory(@PathVariable Long projectId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Velocity history fetched successfully",
                velocityService.getProjectVelocityHistory(projectId)));
    }
}



