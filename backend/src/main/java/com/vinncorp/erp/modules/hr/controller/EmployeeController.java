package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import com.vinncorp.erp.modules.hr.service.EmployeeService;
import com.vinncorp.erp.modules.hr.request.EmployeeCreateRequest;
import com.vinncorp.erp.modules.hr.request.EmployeeUpdateRequest;
import com.vinncorp.erp.modules.hr.response.EmployeeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest request,
                                                    Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.create(request, workspaceId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmployeeResponse>> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(employeeService.list(workspaceId, departmentId, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(employeeService.get(id, workspaceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody EmployeeUpdateRequest request,
                                                    Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(employeeService.update(id, request, workspaceId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        employeeService.delete(id, workspaceId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}


