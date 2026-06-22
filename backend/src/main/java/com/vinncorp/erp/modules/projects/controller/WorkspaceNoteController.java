package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.dto.request.WorkspaceNoteRequest;
import com.vinncorp.erp.platform.workspace.dto.response.WorkspaceNoteResponse;
import com.vinncorp.erp.platform.workspace.service.impl.WorkspaceNoteService;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "Workspace Notes")
public class WorkspaceNoteController {

    private final WorkspaceNoteService workspaceNoteService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List workspace or project notes")
    public ResponseEntity<ApiResponse<Page<WorkspaceNoteResponse>>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Notes fetched",
                workspaceNoteService.list(wsId, projectId, PageRequest.of(page, size))));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceNoteResponse>> create(
            @Valid @RequestBody WorkspaceNoteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Note created",
                workspaceNoteService.create(wsId, userDetails.user().getId(), request)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceNoteResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkspaceNoteRequest request) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Note updated",
                workspaceNoteService.update(wsId, id, request)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        workspaceNoteService.delete(wsId, id);
        return ResponseEntity.ok(ApiResponse.success("Note deleted", null));
    }
}



