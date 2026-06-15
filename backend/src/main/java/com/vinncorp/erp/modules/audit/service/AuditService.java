package com.vinncorp.erp.modules.audit.service;

import com.vinncorp.erp.modules.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface AuditService {

    AuditEvent log(Long workspaceId, Long actorId, String actorEmail,
                   String action, String entityType, Long entityId, String entityName,
                   Map<String, Object> oldValue, Map<String, Object> newValue,
                   String ipAddress, String userAgent);

    AuditEvent log(Long workspaceId, Long actorId, String actorEmail,
                   String action, String entityType, Long entityId, String entityName,
                   Map<String, Object> oldValue, Map<String, Object> newValue,
                   String ipAddress, String userAgent, String metadata);

    Page<AuditEvent> list(Long workspaceId, Pageable pageable);

    Page<AuditEvent> listByAction(Long workspaceId, String action, Pageable pageable);

    Page<AuditEvent> listByEntityType(Long workspaceId, String entityType, Pageable pageable);

    Page<AuditEvent> listByActor(Long workspaceId, Long actorId, Pageable pageable);

    Page<AuditEvent> listByEntity(String entityType, Long entityId, Pageable pageable);

    Page<AuditEvent> listByDateRange(Long workspaceId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditEvent> search(Long workspaceId, String keyword, Pageable pageable);

    java.util.List<AuditEvent> getRecentByEntity(Long workspaceId, String entityType, Long entityId);
}
