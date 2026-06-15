package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.entity.SlackIntegration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackIntegrationResponse {
    private Long id;
    private Long projectId;
    private String workspaceId;
    private String workspaceName;
    private String channelId;
    private String channelName;
    private String botUserId;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static SlackIntegrationResponse fromEntity(SlackIntegration entity) {
        return SlackIntegrationResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProject().getId())
                .workspaceId(entity.getWorkspaceId())
                .workspaceName(entity.getWorkspaceName())
                .channelId(entity.getChannelId())
                .channelName(entity.getChannelName())
                .botUserId(entity.getBotUserId())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}



