package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.request.LeaveTypeCreateRequest;
import com.vinncorp.erp.modules.hr.response.LeaveTypeResponse;
import com.vinncorp.erp.modules.hr.service.LeaveTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/leave-types")
@RequiredArgsConstructor
@Tag(name = "HR Leave Types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping
    @Operation(summary = "Create leave type")
    public ResponseEntity<LeaveTypeResponse> create(@Valid @RequestBody LeaveTypeCreateRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveTypeService.create(request, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List all leave types")
    public ResponseEntity<List<LeaveTypeResponse>> list() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveTypeService.list(workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    @Operation(summary = "List active leave types")
    public ResponseEntity<List<LeaveTypeResponse>> listActive() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveTypeService.listActive(workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get leave type by ID")
    public ResponseEntity<LeaveTypeResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveTypeService.get(id, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update leave type")
    public ResponseEntity<LeaveTypeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody LeaveTypeCreateRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leaveTypeService.update(id, request, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete leave type")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        leaveTypeService.delete(id, workspaceId);
        return ResponseEntity.noContent().build();
    }
}
