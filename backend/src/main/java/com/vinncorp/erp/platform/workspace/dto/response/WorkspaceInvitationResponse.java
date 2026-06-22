package com.vinncorp.erp.platform.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInvitationResponse {
    private Long id;
    private Long workspaceId;
    private String workspaceName;
    private String email;
    private String invitedByName;
    private String workspaceRole;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
}

