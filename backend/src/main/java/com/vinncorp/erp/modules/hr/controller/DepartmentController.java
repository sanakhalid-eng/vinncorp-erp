package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.dto.request.DepartmentCreateRequest;
import com.vinncorp.erp.modules.hr.service.DepartmentService;
import com.vinncorp.erp.modules.hr.dto.response.DepartmentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentCreateRequest request,
                                                      Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.create(request, workspaceId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepartmentResponse>> list(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(departmentService.list(workspaceId, activeOnly));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DepartmentResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(departmentService.get(id, workspaceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<DepartmentResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody DepartmentCreateRequest request,
                                                      Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(departmentService.update(id, request, workspaceId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        departmentService.delete(id, workspaceId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}


