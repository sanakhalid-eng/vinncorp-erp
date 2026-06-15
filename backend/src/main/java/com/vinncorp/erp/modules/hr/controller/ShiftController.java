package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.request.ShiftCreateRequest;
import com.vinncorp.erp.modules.hr.response.ShiftResponse;
import com.vinncorp.erp.modules.hr.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/shifts")
@RequiredArgsConstructor
@Tag(name = "HR Shifts")
public class ShiftController {

    private final ShiftService shiftService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping
    @Operation(summary = "Create shift")
    public ResponseEntity<ShiftResponse> create(@Valid @RequestBody ShiftCreateRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(shiftService.create(request, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List all shifts")
    public ResponseEntity<List<ShiftResponse>> list() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(shiftService.list(workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    @Operation(summary = "List active shifts")
    public ResponseEntity<List<ShiftResponse>> listActive() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(shiftService.listActive(workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get shift by ID")
    public ResponseEntity<ShiftResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(shiftService.get(id, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update shift")
    public ResponseEntity<ShiftResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody ShiftCreateRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(shiftService.update(id, request, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete shift")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        shiftService.delete(id, workspaceId);
        return ResponseEntity.noContent().build();
    }
}
