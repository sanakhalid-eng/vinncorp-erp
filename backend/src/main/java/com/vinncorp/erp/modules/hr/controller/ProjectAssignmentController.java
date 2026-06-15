package com.vinncorp.erp.modules.hr.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.request.ProjectAssignmentRequest;
import com.vinncorp.erp.modules.hr.response.ProjectAssignmentResponse;
import com.vinncorp.erp.modules.hr.service.ProjectAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/project-assignments")
@RequiredArgsConstructor
@Tag(name = "HR Project Assignments")
public class ProjectAssignmentController {

    private final ProjectAssignmentService assignmentService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping
    @Operation(summary = "Assign employee to project")
    public ResponseEntity<ProjectAssignmentResponse> assign(
            @Valid @RequestBody ProjectAssignmentRequest request,
            Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.assign(request, workspaceId, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update project assignment")
    public ResponseEntity<ProjectAssignmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectAssignmentRequest request,
            Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(assignmentService.update(id, request, workspaceId, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @PostMapping("/{id}/unassign")
    @Operation(summary = "Unassign employee from project")
    public ResponseEntity<ProjectAssignmentResponse> unassign(
            @PathVariable Long id,
            Authentication authentication) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(assignmentService.unassign(id, workspaceId, authentication.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID")
    public ResponseEntity<ProjectAssignmentResponse> get(@PathVariable Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(assignmentService.get(id, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get assignments by employee")
    public ResponseEntity<List<ProjectAssignmentResponse>> getByEmployee(@PathVariable Long employeeId) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(assignmentService.getByEmployee(employeeId, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get assignments by project")
    public ResponseEntity<List<ProjectAssignmentResponse>> getByProject(@PathVariable Long projectId) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(assignmentService.getByProject(projectId, workspaceId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Get all assignments")
    public ResponseEntity<List<ProjectAssignmentResponse>> getAll() {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(assignmentService.getAll(workspaceId));
    }
}
