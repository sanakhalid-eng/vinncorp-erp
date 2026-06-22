package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.entity.Lead;
import com.vinncorp.erp.modules.crm.enums.LeadStatus;
import com.vinncorp.erp.modules.crm.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/leads")
@RequiredArgsConstructor
@Tag(name = "CRM Leads")
public class LeadController {

    private final LeadService leadService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('LEAD_CREATE')")
    @Operation(summary = "Create lead")
    public ResponseEntity<Lead> create(@RequestBody Lead lead, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leadService.create(lead, wsId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List leads")
    public ResponseEntity<List<Lead>> list(@RequestParam(required = false) LeadStatus status,
                                           @RequestParam(required = false) String search) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(leadService.search(wsId, search));
        }
        if (status != null) {
            return ResponseEntity.ok(leadService.listByStatus(wsId, status));
        }
        return ResponseEntity.ok(leadService.list(wsId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get lead")
    public ResponseEntity<Lead> get(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leadService.get(id, wsId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('LEAD_UPDATE')")
    @Operation(summary = "Update lead")
    public ResponseEntity<Lead> update(@PathVariable Long id, @RequestBody Lead lead, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leadService.update(id, lead, wsId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('LEAD_DELETE')")
    @Operation(summary = "Delete lead")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        leadService.delete(id, wsId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('LEAD_UPDATE')")
    @Operation(summary = "Convert lead to customer")
    public ResponseEntity<Lead> convert(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leadService.convert(id, wsId, auth.getName()));
    }

    @GetMapping("/count/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Count leads by status")
    public ResponseEntity<Long> countByStatus(@PathVariable LeadStatus status) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(leadService.countByStatus(wsId, status));
    }
}
