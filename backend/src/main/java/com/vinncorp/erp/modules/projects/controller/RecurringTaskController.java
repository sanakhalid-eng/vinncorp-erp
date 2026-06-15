package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.CreateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.request.UpdateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.RecurringOccurrenceResponse;
import com.vinncorp.erp.modules.projects.dto.response.RecurringTemplateResponse;
import com.vinncorp.erp.modules.projects.service.RecurringTaskService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Recurring Tasks")
public class RecurringTaskController {

    private final RecurringTaskService recurringTaskService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/recurring")
    @Operation(summary = "Create recurring schedule", description = "Create a recurring task schedule for a task")
    public ResponseEntity<ApiResponse<RecurringTemplateResponse>> createRecurring(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateRecurringRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recurring schedule created",
                        recurringTaskService.createRecurring(taskId, request, userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/recurring/{id}")
    @Operation(summary = "Update recurring schedule", description = "Update recurring schedule configuration")
    public ResponseEntity<ApiResponse<RecurringTemplateResponse>> updateRecurring(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecurringRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recurring schedule updated",
                        recurringTaskService.updateRecurring(id, request, userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recurring/{id}")
    @Operation(summary = "Get recurring template", description = "Get recurring schedule details")
    public ResponseEntity<ApiResponse<RecurringTemplateResponse>> getRecurringTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recurring template fetched",
                        recurringTaskService.getRecurringTemplate(id))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recurring/project/{projectId}")
    @Operation(summary = "Get project recurring templates", description = "Get all recurring schedules for a project")
    public ResponseEntity<ApiResponse<List<RecurringTemplateResponse>>> getProjectTemplates(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Project recurring templates fetched",
                        recurringTaskService.getTemplatesByProject(projectId))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/recurring/{id}/pause")
    @Operation(summary = "Pause recurring schedule", description = "Pause future task generation")
    public ResponseEntity<ApiResponse<RecurringTemplateResponse>> pauseRecurring(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recurring schedule paused",
                        recurringTaskService.pauseRecurring(id, userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/recurring/{id}/resume")
    @Operation(summary = "Resume recurring schedule", description = "Resume paused recurring schedule")
    public ResponseEntity<ApiResponse<RecurringTemplateResponse>> resumeRecurring(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recurring schedule resumed",
                        recurringTaskService.resumeRecurring(id, userDetails.getUsername()))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/recurring/{id}/stop")
    @Operation(summary = "Stop recurring schedule", description = "Stop future task generation permanently")
    public ResponseEntity<ApiResponse<Void>> stopRecurring(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        recurringTaskService.stopRecurring(id, userDetails.getUsername());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recurring schedule stopped", null)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recurring/{id}/occurrences")
    @Operation(summary = "Get occurrences", description = "Get all generated occurrences for a recurring schedule")
    public ResponseEntity<ApiResponse<List<RecurringOccurrenceResponse>>> getOccurrences(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Occurrences fetched",
                        recurringTaskService.getOccurrences(id))
        );
    }
}



