package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.response.ActivityIntelligenceResponse;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.service.ActivityIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity-intelligence")
@RequiredArgsConstructor
@Tag(name = "Activity Intelligence")
public class ActivityIntelligenceController {

    private final ActivityIntelligenceService activityIntelligenceService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/summary")
    @Operation(summary = "Generate activity intelligence summary")
    public ResponseEntity<ApiResponse<ActivityIntelligenceResponse>> summary(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "7") int days) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Activity summary generated",
                activityIntelligenceService.generateSummary(wsId, projectId, days)));
    }
}



