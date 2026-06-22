package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.service.CalendarIntelligenceService;
import com.vinncorp.erp.modules.projects.service.NotificationIntelligenceService;
import com.vinncorp.erp.modules.projects.service.PersonalProductivityService;
import com.vinncorp.erp.modules.projects.service.QuickActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Productivity Enhancements")
public class PersonalProductivityController {

    private final PersonalProductivityService personalProductivityService;
    private final NotificationIntelligenceService notificationIntelligenceService;
    private final QuickActionService quickActionService;
    private final CalendarIntelligenceService calendarIntelligenceService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/productivity/personal")
    @Operation(summary = "Personal productivity dashboard")
    public ResponseEntity<ApiResponse<PersonalProductivityDashboardResponse>> personalDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Personal productivity fetched",
                personalProductivityService.getDashboard(wsId, userDetails.user().getId())));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/notifications/intelligence")
    public ResponseEntity<ApiResponse<NotificationIntelligenceResponse>> notificationIntelligence(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Notification intelligence fetched",
                notificationIntelligenceService.getIntelligence(userDetails.user().getId())));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/quick-actions")
    public ResponseEntity<ApiResponse<List<QuickActionResponse>>> quickActions() {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Quick actions fetched",
                quickActionService.listForWorkspace(wsId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/projects/{projectId}/calendar-intelligence")
    public ResponseEntity<ApiResponse<CalendarIntelligenceResponse>> calendarIntelligence(
            @PathVariable Long projectId) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Calendar intelligence fetched",
                calendarIntelligenceService.analyze(wsId, projectId)));
    }
}



