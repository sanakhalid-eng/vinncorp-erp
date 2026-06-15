package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.request.DesignationCreateRequest;
import com.vinncorp.erp.modules.hr.service.DesignationService;
import com.vinncorp.erp.modules.hr.response.DesignationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<DesignationResponse> create(@Valid @RequestBody DesignationCreateRequest request,
                                                       Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(designationService.create(request, workspaceId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DesignationResponse>> list(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(designationService.list(workspaceId, activeOnly));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DesignationResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(designationService.get(id, workspaceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<DesignationResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody DesignationCreateRequest request,
                                                       Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(designationService.update(id, request, workspaceId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        designationService.delete(id, workspaceId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}


