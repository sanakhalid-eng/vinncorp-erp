package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.dto.request.CreateInvitationRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.InvitationResponse;
import com.vinncorp.erp.modules.projects.service.InvitationService;
import com.vinncorp.erp.shared.security.WorkspaceOwnerSecurity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserRepository userRepository;
    private final WorkspaceOwnerSecurity workspaceOwnerSecurity;

    @PreAuthorize("hasAuthority('ADD_MEMBER') or hasAuthority('ASSIGN_SYSTEM_ROLE')")
    @PostMapping("/projects/{projectId}/invitations")
    @Operation(summary = "Create invitation", description = "Create an invitation to a project")
    public ResponseEntity<ApiResponse<InvitationResponse>> createInvitation(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateInvitationRequest request,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        InvitationResponse response = invitationService.createInvitation(projectId, request, currentUser.getId());

        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation sent successfully", response));
    }

    @PreAuthorize("hasAuthority('VIEW_MEMBERS') or hasAuthority('ASSIGN_SYSTEM_ROLE')")
    @GetMapping("/projects/{projectId}/invitations")
    @Operation(summary = "Get project invitations", description = "Retrieve all invitations for a project")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getProjectInvitations(
            @PathVariable Long projectId
    ) {
        List<InvitationResponse> invitations = invitationService.getProjectInvitations(projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invitations retrieved", invitations));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/invitations/{id}")
    @Operation(summary = "Revoke invitation", description = "Revoke a pending invitation")
    public ResponseEntity<ApiResponse<Void>> revokeInvitation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        invitationService.revokeInvitation(id, currentUser.getId());

        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation revoked", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/invitations/accept/{token}")
    @Operation(summary = "Accept invitation", description = "Accept a project invitation using a token")
    public ResponseEntity<ApiResponse<InvitationResponse>> acceptInvitation(
            @PathVariable String token,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        InvitationResponse response = invitationService.acceptInvitation(token, currentUser.getId());

        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation accepted", response));
    }

    @GetMapping("/invitations/{token}")
    @Operation(summary = "Get invitation by token", description = "Retrieve invitation details using its token")
    public ResponseEntity<ApiResponse<InvitationResponse>> getInvitation(@PathVariable String token) {
        InvitationResponse response = invitationService.getInvitationByToken(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation retrieved", response));
    }
}



