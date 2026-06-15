package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.request.SavedSearchRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.SavedSearchResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.service.SavedSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-searches")
@RequiredArgsConstructor
@Tag(name = "Saved Searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List saved searches for current user")
    public ResponseEntity<ApiResponse<List<SavedSearchResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Saved searches fetched",
                savedSearchService.list(wsId, userDetails.user().getId())));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<SavedSearchResponse>> create(
            @Valid @RequestBody SavedSearchRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Saved search created",
                savedSearchService.create(wsId, userDetails.user().getId(), request)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SavedSearchRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Saved search updated",
                savedSearchService.update(wsId, userDetails.user().getId(), id, request)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        savedSearchService.delete(wsId, userDetails.user().getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Saved search deleted", null));
    }
}



