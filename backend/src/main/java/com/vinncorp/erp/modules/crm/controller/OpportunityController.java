package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.entity.Opportunity;
import com.vinncorp.erp.modules.crm.service.OpportunityService;
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
@RequestMapping("/api/crm/opportunities")
@RequiredArgsConstructor
@Tag(name = "CRM Opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('DEAL_CREATE')")
    @Operation(summary = "Create opportunity")
    public ResponseEntity<Opportunity> create(@RequestBody Opportunity opp, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opportunityService.create(opp, wsId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List opportunities")
    public ResponseEntity<List<Opportunity>> list(@RequestParam(required = false) Long stageId,
                                                  @RequestParam(required = false) String view) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        if ("open".equals(view)) {
            return ResponseEntity.ok(opportunityService.listOpen(wsId));
        }
        if (stageId != null) {
            return ResponseEntity.ok(opportunityService.listByStage(wsId, stageId));
        }
        return ResponseEntity.ok(opportunityService.list(wsId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get opportunity")
    public ResponseEntity<Opportunity> get(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(opportunityService.get(id, wsId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('DEAL_UPDATE')")
    @Operation(summary = "Update opportunity")
    public ResponseEntity<Opportunity> update(@PathVariable Long id, @RequestBody Opportunity opp, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(opportunityService.update(id, opp, wsId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('DEAL_DELETE')")
    @Operation(summary = "Delete opportunity")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        opportunityService.delete(id, wsId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stage/{stageId}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('DEAL_UPDATE')")
    @Operation(summary = "Change opportunity stage")
    public ResponseEntity<Opportunity> changeStage(@PathVariable Long id, @PathVariable Long stageId, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(opportunityService.changeStage(id, stageId, wsId, auth.getName()));
    }

    @PostMapping("/{id}/won")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('DEAL_UPDATE')")
    @Operation(summary = "Mark opportunity as won (auto-creates project)")
    public ResponseEntity<Opportunity> won(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(opportunityService.won(id, wsId, auth.getName()));
    }

    @PostMapping("/{id}/lost")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('DEAL_UPDATE')")
    @Operation(summary = "Mark opportunity as lost")
    public ResponseEntity<Opportunity> lost(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(opportunityService.lost(id, wsId, auth.getName()));
    }
}
