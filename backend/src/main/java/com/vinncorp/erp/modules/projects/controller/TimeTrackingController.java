package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.TimeLogRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.ActiveTimer;
import com.vinncorp.erp.modules.projects.entity.TimeLog;
import com.vinncorp.erp.modules.projects.service.TimeTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Time Tracking")
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/time-logs")
    @Operation(summary = "Log time", description = "Log time spent on a task")
    public ResponseEntity<ApiResponse<TimeLog>> logTime(
            @PathVariable Long taskId,
            @RequestParam Long userId,
            @RequestBody TimeLogRequest request) {
        TimeLog timeLog = timeTrackingService.logTime(taskId, userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Time logged successfully", timeLog));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/{taskId}/time-logs")
    @Operation(summary = "Get task time logs", description = "Retrieve all time logs for a task")
    public ResponseEntity<ApiResponse<java.util.List<TimeLog>>> getTaskTimeLogs(@PathVariable Long taskId) {
        java.util.List<TimeLog> logs = timeTrackingService.getTaskTimeLogs(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Time logs fetched successfully", logs));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/time-logs/{id}")
    @Operation(summary = "Update time log", description = "Update an existing time log entry")
    public ResponseEntity<ApiResponse<TimeLog>> updateTimeLog(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestBody TimeLogRequest request) {
        TimeLog timeLog = timeTrackingService.updateTimeLog(id, userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Time log updated successfully", timeLog));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/time-logs/{id}")
    @Operation(summary = "Delete time log", description = "Delete a time log entry by ID")
    public ResponseEntity<ApiResponse<Void>> deleteTimeLog(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        timeTrackingService.deleteTimeLog(id, userId, isAdmin);
        return ResponseEntity.ok(new ApiResponse<>(true, "Time log deleted successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/{userId}/timesheet")
    @Operation(summary = "Get user timesheet", description = "Retrieve timesheet for a user by weekly or monthly range")
    public ResponseEntity<ApiResponse<java.util.List<Object[]>>> getUserTimesheet(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "weekly") String range) {
        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate;

        if ("monthly".equalsIgnoreCase(range)) {
            startDate = endDate.minusMonths(1);
        } else {
            startDate = endDate.minusWeeks(1);
        }

        java.util.List<Object[]> timesheet = timeTrackingService.getUserTimesheet(userId, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet fetched successfully", timesheet));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/{taskId}/time-summary")
    @Operation(summary = "Get task time summary", description = "Get total hours logged for a task")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getTaskTimeSummary(@PathVariable Long taskId) {
        java.math.BigDecimal totalHours = timeTrackingService.getTaskTotalHours(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Task time summary fetched successfully", totalHours));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/timer/start")
    @Operation(summary = "Start timer", description = "Start a timer for a task")
    public ResponseEntity<ApiResponse<ActiveTimer>> startTimer(
            @PathVariable Long taskId,
            @RequestParam Long userId,
            @RequestBody(required = false) TimeLogRequest request) {
        String description = request != null ? request.getDescription() : null;
        ActiveTimer timer = timeTrackingService.startTimer(taskId, userId, description);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timer started successfully", timer));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/timer/stop")
    @Operation(summary = "Stop timer", description = "Stop the active timer and log time")
    public ResponseEntity<ApiResponse<TimeLog>> stopTimer(
            @RequestParam Long userId,
            @RequestBody(required = false) TimeLogRequest request) {
        String description = request != null ? request.getDescription() : null;
        TimeLog timeLog = timeTrackingService.stopTimer(userId, description);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timer stopped successfully", timeLog));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/timer/active")
    @Operation(summary = "Get active timer", description = "Retrieve the active timer for a user")
    public ResponseEntity<ApiResponse<Optional<ActiveTimer>>> getActiveTimer(@RequestParam Long userId) {
        Optional<ActiveTimer> timer = timeTrackingService.getActiveTimer(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Active timer fetched successfully", timer));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/timer/has-active")
    @Operation(summary = "Check active timer", description = "Check if a user has an active timer running")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveTimer(@RequestParam Long userId) {
        boolean hasActive = timeTrackingService.hasActiveTimer(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Active timer status fetched successfully", hasActive));
    }

}



