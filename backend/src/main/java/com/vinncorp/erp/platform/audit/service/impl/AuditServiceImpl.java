package com.vinncorp.erp.platform.audit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.platform.audit.entity.AuditEvent;
import com.vinncorp.erp.platform.audit.repository.AuditEventRepository;
import com.vinncorp.erp.platform.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AuditEvent log(Long workspaceId, Long actorId, String actorEmail,
                          String action, String entityType, Long entityId, String entityName,
                          Map<String, Object> oldValue, Map<String, Object> newValue,
                          String ipAddress, String userAgent) {
        return log(workspaceId, actorId, actorEmail, action, entityType, entityId, entityName,
                oldValue, newValue, ipAddress, userAgent, null);
    }

    @Override
    @Transactional
    public AuditEvent log(Long workspaceId, Long actorId, String actorEmail,
                          String action, String entityType, Long entityId, String entityName,
                          Map<String, Object> oldValue, Map<String, Object> newValue,
                          String ipAddress, String userAgent, String metadata) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .workspaceId(workspaceId)
                    .actorId(actorId)
                    .actorEmail(actorEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .metadata(metadata)
                    .build();
            return auditEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to create audit event: {} {} {}", action, entityType, entityId, e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> list(Long workspaceId, Pageable pageable) {
        return auditEventRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> listByAction(Long workspaceId, String action, Pageable pageable) {
        return auditEventRepository.findByWorkspaceIdAndActionOrderByCreatedAtDesc(workspaceId, action, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> listByEntityType(Long workspaceId, String entityType, Pageable pageable) {
        return auditEventRepository.findByWorkspaceIdAndEntityTypeOrderByCreatedAtDesc(workspaceId, entityType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> listByActor(Long workspaceId, Long actorId, Pageable pageable) {
        return auditEventRepository.findByWorkspaceIdAndActorIdOrderByCreatedAtDesc(workspaceId, actorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> listByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> listByDateRange(Long workspaceId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditEventRepository.findByDateRange(workspaceId, start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> search(Long workspaceId, String keyword, Pageable pageable) {
        return auditEventRepository.searchByKeyword(workspaceId, keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<AuditEvent> getRecentByEntity(Long workspaceId, String entityType, Long entityId) {
        return auditEventRepository.findTop50ByWorkspaceIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                workspaceId, entityType, entityId);
    }
}
