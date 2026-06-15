package com.vinncorp.erp.core.workspace.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String avatarUrl;
    private String workspaceRole;
    private LocalDateTime joinedAt;
    private boolean active;
}

