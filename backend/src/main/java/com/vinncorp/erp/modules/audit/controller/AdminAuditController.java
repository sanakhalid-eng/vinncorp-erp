package com.vinncorp.erp.modules.audit.controller;

import com.vinncorp.erp.modules.audit.dto.response.AuditEventResponse;
import com.vinncorp.erp.modules.audit.entity.AuditEvent;
import com.vinncorp.erp.modules.audit.repository.AuditEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin Audit Logs")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminAuditController {

    private final AuditEventRepository auditEventRepository;

    @GetMapping
    @Operation(summary = "Get all audit logs with filters (admin)")
    public ResponseEntity<Page<AuditEventResponse>> getAuditLogs(
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditEvent> events = auditEventRepository.findByFilters(
                workspaceId, userId, entityType, action, startDate, endDate, pageable);

        return ResponseEntity.ok(events.map(AuditEventResponse::from));
    }
}
