package com.vinncorp.erp.platform.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {
    private Long id;
    private Long workspaceId;
    private Long actorId;
    private String actorEmail;
    private String action;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String metadata;
    private LocalDateTime createdAt;

    public static AuditEventResponse from(com.vinncorp.erp.platform.audit.entity.AuditEvent e) {
        if (e == null) return null;
        return AuditEventResponse.builder()
                .id(e.getId())
                .workspaceId(e.getWorkspaceId())
                .actorId(e.getActorId())
                .actorEmail(e.getActorEmail())
                .action(e.getAction())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .entityName(e.getEntityName())
                .oldValue(e.getOldValue())
                .newValue(e.getNewValue())
                .ipAddress(e.getIpAddress())
                .metadata(e.getMetadata())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
