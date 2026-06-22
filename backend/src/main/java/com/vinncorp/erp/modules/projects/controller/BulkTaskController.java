package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.request.BulkTaskUpdateRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.BulkTaskUpdateResponse;
import com.vinncorp.erp.modules.projects.service.BulkTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Bulk Tasks")
public class BulkTaskController {

    private final BulkTaskService bulkTaskService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/bulk")
    @Operation(summary = "Bulk update tasks")
    public ResponseEntity<ApiResponse<BulkTaskUpdateResponse>> bulkUpdate(
            @Valid @RequestBody BulkTaskUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Bulk update completed",
                bulkTaskService.bulkUpdate(wsId, request, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk delete tasks")
    public ResponseEntity<ApiResponse<BulkTaskUpdateResponse>> bulkDelete(
            @Valid @RequestBody BulkTaskUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed",
                bulkTaskService.bulkDelete(wsId, request, userDetails.getUsername())));
    }
}



