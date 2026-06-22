package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.enums.LeaveRequestStatus;
import com.vinncorp.erp.modules.hr.dto.request.LeaveRequestActionRequest;
import com.vinncorp.erp.modules.hr.dto.request.LeaveRequestCreateRequest;
import com.vinncorp.erp.modules.hr.dto.response.LeaveRequestResponse;
import com.vinncorp.erp.modules.hr.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr/leave-requests")
@RequiredArgsConstructor
@Tag(name = "HR Leave Requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Apply for leave")
    public ResponseEntity<LeaveRequestResponse> apply(
            @Valid @RequestBody LeaveRequestCreateRequest request,
            Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(leaveRequestService.apply(request, workspaceId, userId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve leave request")
    public ResponseEntity<LeaveRequestResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(leaveRequestService.approve(id, workspaceId, userId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject leave request")
    public ResponseEntity<LeaveRequestResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveRequestActionRequest request,
            Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(leaveRequestService.reject(id, request, workspaceId, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel leave request")
    public ResponseEntity<LeaveRequestResponse> cancel(
            @PathVariable Long id,
            Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(leaveRequestService.cancel(id, workspaceId, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID")
    public ResponseEntity<LeaveRequestResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveRequestService.get(id, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List all leave requests (filtered by status)")
    public ResponseEntity<List<LeaveRequestResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        if (employeeId != null) {
            return ResponseEntity.ok(leaveRequestService.listByEmployee(employeeId, workspaceId));
        }
        if (status != null) {
            try {
                LeaveRequestStatus leaveStatus = LeaveRequestStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(leaveRequestService.listByStatus(leaveStatus, workspaceId));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.ok(leaveRequestService.listAll(workspaceId));
            }
        }
        return ResponseEntity.ok(leaveRequestService.listAll(workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard")
    @Operation(summary = "Get leave dashboard counts")
    public ResponseEntity<Map<String, Long>> dashboard() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        long pending = leaveRequestService.countPending(workspaceId);
        return ResponseEntity.ok(Map.of("pendingCount", pending));
    }

    private Long getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        return null;
    }
}
