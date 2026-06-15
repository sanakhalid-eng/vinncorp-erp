package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.TimesheetApproval;
import com.vinncorp.erp.modules.projects.service.TimeTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
@RequiredArgsConstructor
@Tag(name = "Time Tracking")
public class TimesheetApprovalController {

    private final TimeTrackingService timeTrackingService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/submit")
    @Operation(summary = "Submit timesheet", description = "Submit a timesheet for approval")
    public ResponseEntity<ApiResponse<TimesheetApproval>> submitTimesheet(
            @RequestParam Long userId,
            @RequestParam String weekStart) {
        TimesheetApproval approval = timeTrackingService.submitTimesheet(userId, java.time.LocalDate.parse(weekStart));
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet submitted successfully", approval));
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve timesheet", description = "Approve a pending timesheet")
    public ResponseEntity<ApiResponse<TimesheetApproval>> approveTimesheet(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        TimesheetApproval approval = timeTrackingService.approveTimesheet(id, approverId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet approved successfully", approval));
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject timesheet", description = "Reject a pending timesheet with an optional reason")
    public ResponseEntity<ApiResponse<TimesheetApproval>> rejectTimesheet(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam(required = false) String reason) {
        TimesheetApproval approval = timeTrackingService.rejectTimesheet(id, approverId, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet rejected successfully", approval));
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/pending")
    @Operation(summary = "Get pending timesheets", description = "Retrieve all timesheets pending approval")
    public ResponseEntity<ApiResponse<List<TimesheetApproval>>> getPendingTimesheets() {
        List<TimesheetApproval> approvals = timeTrackingService.getPendingApprovals();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending timesheets fetched successfully", approvals));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user timesheets", description = "Retrieve all timesheets for a specific user")
    public ResponseEntity<ApiResponse<List<TimesheetApproval>>> getUserTimesheets(@PathVariable Long userId) {
        List<TimesheetApproval> approvals = timeTrackingService.getUserTimesheets(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "User timesheets fetched successfully", approvals));
    }
}



