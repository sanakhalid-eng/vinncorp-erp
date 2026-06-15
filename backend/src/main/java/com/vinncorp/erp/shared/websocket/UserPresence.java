package com.vinncorp.erp.shared.websocket;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserPresence {
    private Long userId;
    private String userName;
    private String userEmail;
    private String avatarUrl;
    private Long workspaceId;
    private LocalDateTime lastActive;
    private boolean online;
    private String status;
}

