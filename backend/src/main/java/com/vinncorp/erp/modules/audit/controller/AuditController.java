package com.vinncorp.erp.modules.audit.controller;

import com.vinncorp.erp.modules.audit.dto.response.AuditEventResponse;
import com.vinncorp.erp.modules.audit.entity.AuditEvent;
import com.vinncorp.erp.modules.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/crm/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Trail")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "List audit events")
    public ResponseEntity<Page<AuditEventResponse>> list(
            @RequestParam Long workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        var pageable = PageRequest.of(page, size);
        Page<AuditEvent> events;

        if (keyword != null && !keyword.isBlank()) {
            events = auditService.search(workspaceId, keyword, pageable);
        } else if (startDate != null && endDate != null) {
            events = auditService.listByDateRange(workspaceId, startDate, endDate, pageable);
        } else if (action != null && !action.isBlank()) {
            events = auditService.listByAction(workspaceId, action, pageable);
        } else if (entityType != null && !entityType.isBlank()) {
            events = auditService.listByEntityType(workspaceId, entityType, pageable);
        } else if (actorId != null) {
            events = auditService.listByActor(workspaceId, actorId, pageable);
        } else {
            events = auditService.list(workspaceId, pageable);
        }

        return ResponseEntity.ok(events.map(AuditEventResponse::from));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit history for a specific entity")
    public ResponseEntity<?> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam Long workspaceId) {
        var events = auditService.getRecentByEntity(workspaceId, entityType, entityId);
        return ResponseEntity.ok(events.stream().map(AuditEventResponse::from).toList());
    }
}
