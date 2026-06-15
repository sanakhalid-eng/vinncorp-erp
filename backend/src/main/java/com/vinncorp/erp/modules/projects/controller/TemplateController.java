package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.CreateProjectFromTemplateRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.ProjectResponse;
import com.vinncorp.erp.modules.projects.entity.ProjectTemplate;
import com.vinncorp.erp.modules.projects.service.ProjectTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Tag(name = "Templates")
public class TemplateController {

    private final ProjectTemplateService projectTemplateService;

    @GetMapping
    @Operation(summary = "List project templates")
    public ResponseEntity<ApiResponse<List<ProjectTemplate>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success("Templates retrieved", projectTemplateService.getTemplates()));
    }

    @PostMapping("/{templateId}/create-project")
    @Operation(summary = "Create project from template")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProjectFromTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody CreateProjectFromTemplateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProjectResponse project = projectTemplateService.createProjectFromTemplate(templateId, request, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Project created from template", project));
    }
}



