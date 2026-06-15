package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.core.workspace.request.CreateWorkspaceRequest;
import com.vinncorp.erp.core.workspace.request.WorkspacePreferencesRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.service.WorkspaceService;
import org.springframework.web.multipart.MultipartFile;
import com.vinncorp.erp.core.workspace.response.WorkspaceMemberResponse;
import com.vinncorp.erp.core.workspace.response.WorkspaceResponse;
import com.vinncorp.erp.core.workspace.response.WorkspaceSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get workspace by slug")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceResponse response = workspaceService.getWorkspaceBySlug(slug, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved", response));
    }

    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceResponse response = workspaceService.createWorkspace(request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Workspace created", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace by ID")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceResponse response = workspaceService.getWorkspace(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved", response));
    }

    @GetMapping
    @Operation(summary = "List user's workspaces")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getUserWorkspaces(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved", workspaces));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Long id,
            @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceResponse response = workspaceService.updateWorkspace(id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Workspace updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workspace")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        workspaceService.deleteWorkspace(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted"));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get workspace members")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getWorkspaceMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<WorkspaceMemberResponse> members = workspaceService.getWorkspaceMembers(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Members retrieved", members));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove workspace member")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        workspaceService.removeMember(id, userId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Member removed"));
    }

    @GetMapping("/{id}/settings")
    @Operation(summary = "Get workspace settings")
    public ResponseEntity<ApiResponse<WorkspaceSettingsResponse>> getWorkspaceSettings(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceSettingsResponse settings = workspaceService.getWorkspaceSettings(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Settings retrieved", settings));
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload workspace logo")
    public ResponseEntity<ApiResponse<String>> uploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        String url = workspaceService.uploadLogo(id, userDetails.getUserId(), file);
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded", url));
    }

    @PostMapping("/{id}/switch")
    @Operation(summary = "Switch active workspace")
    public ResponseEntity<ApiResponse<Void>> switchWorkspace(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        workspaceService.switchWorkspace(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Workspace switched"));
    }

    @GetMapping("/{id}/preferences")
    @Operation(summary = "Get workspace preferences")
    public ResponseEntity<ApiResponse<WorkspaceSettingsResponse.WorkspacePreferences>> getWorkspacePreferences(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceSettingsResponse settings = workspaceService.getWorkspaceSettings(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Preferences retrieved", settings.getPreferences()));
    }

    @PutMapping("/{id}/preferences")
    @Operation(summary = "Update workspace preferences")
    public ResponseEntity<ApiResponse<WorkspaceSettingsResponse.WorkspacePreferences>> updateWorkspacePreferences(
            @PathVariable Long id,
            @RequestBody WorkspacePreferencesRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceSettingsResponse.WorkspacePreferences prefs =
                workspaceService.updateWorkspacePreferences(id, userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", prefs));
    }
}