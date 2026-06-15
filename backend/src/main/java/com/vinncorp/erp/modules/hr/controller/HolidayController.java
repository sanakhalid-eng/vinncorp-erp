package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.request.HolidayCreateRequest;
import com.vinncorp.erp.modules.hr.response.HolidayResponse;
import com.vinncorp.erp.modules.hr.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hr/holidays")
@RequiredArgsConstructor
@Tag(name = "HR Holidays")
public class HolidayController {

    private final HolidayService holidayService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping
    @Operation(summary = "Create holiday")
    public ResponseEntity<HolidayResponse> create(@Valid @RequestBody HolidayCreateRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(holidayService.create(request, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List all holidays")
    public ResponseEntity<List<HolidayResponse>> list() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(holidayService.list(workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/range")
    @Operation(summary = "Get holidays by date range")
    public ResponseEntity<List<HolidayResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(holidayService.getByDateRange(workspaceId, startDate, endDate));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get holiday by ID")
    public ResponseEntity<HolidayResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(holidayService.get(id, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update holiday")
    public ResponseEntity<HolidayResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody HolidayCreateRequest request) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(holidayService.update(id, request, workspaceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete holiday")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        holidayService.delete(id, workspaceId);
        return ResponseEntity.noContent().build();
    }
}
