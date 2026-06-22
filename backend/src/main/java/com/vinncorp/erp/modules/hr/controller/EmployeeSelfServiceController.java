package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.dto.response.*;
import com.vinncorp.erp.modules.hr.service.AttendanceService;
import com.vinncorp.erp.modules.hr.service.EmployeeService;
import com.vinncorp.erp.modules.hr.service.LeaveBalanceService;
import com.vinncorp.erp.modules.hr.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr/self")
@RequiredArgsConstructor
@Tag(name = "Employee Self-Service")
public class EmployeeSelfServiceController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveRequestService;
    private final LeaveBalanceService leaveBalanceService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    @Operation(summary = "Get current employee's profile")
    public ResponseEntity<EmployeeResponse> getMyProfile(Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        User user = (User) authentication.getPrincipal();
        EmployeeResponse employee = employeeService.getByUserId(user.getId(), workspaceId);
        return ResponseEntity.ok(employee);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/attendance")
    @Operation(summary = "Get current employee's attendance summary")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        User user = (User) authentication.getPrincipal();
        EmployeeResponse employee = employeeService.getByUserId(user.getId(), workspaceId);
        List<AttendanceResponse> attendance = attendanceService.getEmployeeAttendance(
                employee.getId(), workspaceId, startDate, endDate);
        return ResponseEntity.ok(attendance);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/attendance/summary")
    @Operation(summary = "Get current employee's attendance summary for a month")
    public ResponseEntity<Map<String, Object>> getMyAttendanceSummary(
            Authentication authentication,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        User user = (User) authentication.getPrincipal();
        EmployeeResponse employee = employeeService.getByUserId(user.getId(), workspaceId);

        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        LocalDate startDate = LocalDate.of(y, m, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<AttendanceResponse> records = attendanceService.getEmployeeAttendance(
                employee.getId(), workspaceId, startDate, endDate);

        long presentCount = records.stream().filter(r -> "PRESENT".equals(r.getStatus())).count();
        long absentCount = records.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long lateCount = records.stream().filter(r -> "LATE".equals(r.getStatus())).count();
        long onLeaveCount = records.stream().filter(r -> "ON_LEAVE".equals(r.getStatus())).count();
        long halfDayCount = records.stream().filter(r -> "HALF_DAY".equals(r.getStatus())).count();

        return ResponseEntity.ok(Map.of(
                "year", y,
                "month", m,
                "totalDays", records.size(),
                "presentCount", presentCount,
                "absentCount", absentCount,
                "lateCount", lateCount,
                "onLeaveCount", onLeaveCount,
                "halfDayCount", halfDayCount
        ));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/leaves")
    @Operation(summary = "Get current employee's leave requests")
    public ResponseEntity<List<LeaveRequestResponse>> getMyLeaves(Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        User user = (User) authentication.getPrincipal();
        EmployeeResponse employee = employeeService.getByUserId(user.getId(), workspaceId);
        return ResponseEntity.ok(leaveRequestService.listByEmployee(employee.getId(), workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/leaves/balance")
    @Operation(summary = "Get current employee's leave balances")
    public ResponseEntity<List<LeaveBalanceResponse>> getMyLeaveBalances(
            Authentication authentication,
            @RequestParam(required = false) Integer year) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        User user = (User) authentication.getPrincipal();
        EmployeeResponse employee = employeeService.getByUserId(user.getId(), workspaceId);
        int effectiveYear = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(leaveBalanceService.getBalancesByEmployee(employee.getId(), effectiveYear, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/documents")
    @Operation(summary = "Get current employee's documents (profile info)")
    public ResponseEntity<Map<String, Object>> getMyDocuments(Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        User user = (User) authentication.getPrincipal();
        EmployeeResponse employee = employeeService.getByUserId(user.getId(), workspaceId);
        return ResponseEntity.ok(Map.of(
                "employee", employee,
                "documents", List.of(
                        Map.of("type", "EMPLOYMENT_CONTRACT", "name", "Employment Contract", "status", "Available"),
                        Map.of("type", "ID_PROOF", "name", "ID Proof", "status", "Pending Upload"),
                        Map.of("type", "ADDRESS_PROOF", "name", "Address Proof", "status", "Pending Upload"),
                        Map.of("type", "QUALIFICATION", "name", "Qualification Certificates", "status", "Pending Upload")
                )
        ));
    }
}
