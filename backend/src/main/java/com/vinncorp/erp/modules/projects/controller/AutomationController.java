package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.SmartAssignmentResponse;
import com.vinncorp.erp.modules.projects.service.DeadlineAutomationService;
import com.vinncorp.erp.modules.projects.service.SmartAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/automation")
@RequiredArgsConstructor
@Tag(name = "Automation")
public class AutomationController {

    private final SmartAssignmentService smartAssignmentService;
    private final DeadlineAutomationService deadlineAutomationService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/auto-assign")
    @Operation(summary = "Auto-assign task", description = "Smart assign a task to the best team member")
    public ResponseEntity<ApiResponse<SmartAssignmentResponse>> autoAssign(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Task auto-assigned successfully",
                smartAssignmentService.autoAssign(taskId, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/shift-due-date")
    @Operation(summary = "Shift due date", description = "Auto-shift due date by days")
    public ResponseEntity<ApiResponse<Void>> shiftDueDate(
            @PathVariable Long taskId,
            @RequestParam int days
    ) {
        deadlineAutomationService.autoShiftDueDates(taskId, days);
        return ResponseEntity.ok(new ApiResponse<>(true, "Due date shifted successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/reschedule-chain")
    @Operation(summary = "Reschedule dependency chain", description = "Reschedule dependent tasks due to date change")
    public ResponseEntity<ApiResponse<Void>> rescheduleChain(
            @PathVariable Long taskId
    ) {
        deadlineAutomationService.rescheduleDependencyChain(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dependency chain rescheduled successfully", null));
    }
}



