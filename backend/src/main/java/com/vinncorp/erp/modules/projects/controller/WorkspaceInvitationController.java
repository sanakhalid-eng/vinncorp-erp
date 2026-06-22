package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.dto.request.CreateWorkspaceInvitationRequest;
import com.vinncorp.erp.platform.workspace.dto.response.WorkspaceInvitationResponse;
import com.vinncorp.erp.platform.workspace.service.impl.WorkspaceInvitationService;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Workspaces")
public class WorkspaceInvitationController {

    private final WorkspaceInvitationService workspaceInvitationService;

    @PostMapping("/api/workspaces/{id}/invitations")
    @Operation(summary = "Create workspace invitation")
    public ResponseEntity<ApiResponse<WorkspaceInvitationResponse>> createInvitation(
            @PathVariable Long id,
            @RequestBody CreateWorkspaceInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceInvitationResponse response = workspaceInvitationService.createInvitation(id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Invitation created", response));
    }

    @GetMapping("/api/workspaces/{id}/invitations")
    @Operation(summary = "List workspace invitations")
    public ResponseEntity<ApiResponse<List<WorkspaceInvitationResponse>>> getInvitations(
            @PathVariable Long id) {
        List<WorkspaceInvitationResponse> invitations = workspaceInvitationService.getWorkspaceInvitations(id);
        return ResponseEntity.ok(ApiResponse.success("Invitations retrieved", invitations));
    }

    @DeleteMapping("/api/workspace-invitations/{id}")
    @Operation(summary = "Revoke workspace invitation")
    public ResponseEntity<ApiResponse<Void>> revokeInvitation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        workspaceInvitationService.revokeInvitation(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Invitation revoked"));
    }

    @PostMapping("/api/workspace-invitations/accept/{token}")
    @Operation(summary = "Accept workspace invitation")
    public ResponseEntity<ApiResponse<WorkspaceInvitationResponse>> acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WorkspaceInvitationResponse response = workspaceInvitationService.acceptInvitation(token, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted", response));
    }

    @GetMapping("/api/workspace-invitations/{token}")
    @Operation(summary = "Get invitation by token")
    public ResponseEntity<ApiResponse<WorkspaceInvitationResponse>> getInvitationByToken(
            @PathVariable String token) {
        WorkspaceInvitationResponse response = workspaceInvitationService.getInvitationByToken(token);
        return ResponseEntity.ok(ApiResponse.success("Invitation retrieved", response));
    }
}


