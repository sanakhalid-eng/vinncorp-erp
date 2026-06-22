package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceCheckInRequest;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceCheckOutRequest;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceUpdateRequest;
import com.vinncorp.erp.modules.hr.dto.response.AttendanceDashboardResponse;
import com.vinncorp.erp.modules.hr.dto.response.AttendanceResponse;
import com.vinncorp.erp.modules.hr.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hr/attendance")
@RequiredArgsConstructor
@Tag(name = "HR Attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping("/check-in")
    @Operation(summary = "Check in employee")
    public ResponseEntity<AttendanceResponse> checkIn(
            @Valid @RequestBody AttendanceCheckInRequest request,
            HttpServletRequest httpRequest) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(attendanceService.checkIn(request, workspaceId, ip));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping("/check-out")
    @Operation(summary = "Check out employee")
    public ResponseEntity<AttendanceResponse> checkOut(
            @Valid @RequestBody AttendanceCheckOutRequest request,
            HttpServletRequest httpRequest) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(attendanceService.checkOut(request, workspaceId, ip));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get attendance by ID")
    public ResponseEntity<AttendanceResponse> getAttendance(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(attendanceService.getAttendance(id, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get employee attendance range")
    public ResponseEntity<List<AttendanceResponse>> getEmployeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(employeeId, workspaceId, startDate, endDate));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/date/{date}")
    @Operation(summary = "Get attendance by date")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard")
    @Operation(summary = "Get attendance dashboard")
    public ResponseEntity<AttendanceDashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(attendanceService.getDashboard(
                date != null ? date : LocalDate.now(), workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update attendance")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        attendanceService.deleteAttendance(id, workspaceId);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
