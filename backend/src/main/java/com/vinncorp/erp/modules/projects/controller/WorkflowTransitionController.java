package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.WorkflowTransitionRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowTransitionResponse;
import com.vinncorp.erp.modules.projects.service.WorkflowTransitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/workflow/transitions")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class WorkflowTransitionController {

    private final WorkflowTransitionService workflowTransitionService;

    @PostMapping
    @Operation(summary = "Create transition", description = "Create a new workflow transition for a project")
    public ResponseEntity<ApiResponse<WorkflowTransitionResponse>> create(
            @PathVariable Long projectId,
            @RequestBody WorkflowTransitionRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transition created",
                        workflowTransitionService.createTransition(projectId, request))
        );
    }

    @GetMapping
    @Operation(summary = "Get all transitions", description = "Retrieve all workflow transitions for a project")
    public ResponseEntity<ApiResponse<List<WorkflowTransitionResponse>>> getAll(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transitions fetched",
                        workflowTransitionService.getTransitions(projectId))
        );
    }

    @DeleteMapping("/{transitionId}")
    @Operation(summary = "Delete transition", description = "Delete a workflow transition by ID")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long projectId,
            @PathVariable Long transitionId
    ) {
        workflowTransitionService.deleteTransition(projectId, transitionId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transition deleted", null)
        );
    }
}



