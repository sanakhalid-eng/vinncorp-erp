package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.CreateProjectRequest;
import com.vinncorp.erp.modules.projects.dto.request.SaveProjectAsTemplateRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.ProjectResponse;
import com.vinncorp.erp.modules.projects.entity.ProjectTemplate;
import com.vinncorp.erp.modules.projects.service.ProjectService;
import com.vinncorp.erp.modules.projects.service.ProjectTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectTemplateService projectTemplateService;

    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    @PostMapping
    @Operation(summary = "Create project", description = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponse project = projectService.createProject(request, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Project created successfully", project));
    }

    @PreAuthorize("hasAuthority('VIEW_ALL_PROJECTS')")
    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieve all projects (admin/PM only)")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(){
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(new ApiResponse<>(true, "Projects fetched successfully", projects));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user-projects")
    @Operation(summary = "Get my projects", description = "Retrieve projects for the current user")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects(@AuthenticationPrincipal UserDetails userDetails) {
        List<ProjectResponse> projects = projectService.getProjectsForUser(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Projects fetched successfully", projects));
    }

    @PreAuthorize("hasAuthority('VIEW_PROJECT')")
    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a single project by its ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id){
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Project fetched successfully", project));
    }

    @PreAuthorize("hasAuthority('EDIT_PROJECT')")
    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Update an existing project")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @RequestBody CreateProjectRequest request){

        ProjectResponse project = projectService.updateProject(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Project updated successfully", project));
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Delete a project by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id){
        projectService.deleteProject(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Project deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('EDIT_PROJECT')")
    @PostMapping("/{id}/save-template")
    @Operation(summary = "Save project as template")
    public ResponseEntity<ApiResponse<ProjectTemplate>> saveProjectAsTemplate(
            @PathVariable Long id,
            @Valid @RequestBody SaveProjectAsTemplateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProjectTemplate template = projectTemplateService.saveProjectAsTemplate(id, request, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Project saved as template", template));
    }
}



