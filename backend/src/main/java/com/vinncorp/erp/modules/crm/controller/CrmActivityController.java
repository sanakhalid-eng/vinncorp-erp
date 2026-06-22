package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.entity.CrmActivity;
import com.vinncorp.erp.modules.crm.service.CrmActivityService;
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
@RequestMapping("/api/crm/activities")
@RequiredArgsConstructor
@Tag(name = "CRM Activities")
public class CrmActivityController {

    private final CrmActivityService crmActivityService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create activity")
    public ResponseEntity<CrmActivity> create(@RequestBody CrmActivity activity, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(crmActivityService.create(activity, wsId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List recent activities")
    public ResponseEntity<List<CrmActivity>> listRecent() {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(crmActivityService.listRecent(wsId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get activity")
    public ResponseEntity<CrmActivity> get(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(crmActivityService.get(id, wsId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update activity")
    public ResponseEntity<CrmActivity> update(@PathVariable Long id, @RequestBody CrmActivity activity, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(crmActivityService.update(id, activity, wsId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete activity")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        crmActivityService.delete(id, wsId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lead/{leadId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List activities for a lead")
    public ResponseEntity<List<CrmActivity>> listByLead(@PathVariable Long leadId) {
        return ResponseEntity.ok(crmActivityService.listByLead(leadId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List activities for a customer")
    public ResponseEntity<List<CrmActivity>> listByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(crmActivityService.listByCustomer(customerId));
    }

    @GetMapping("/contact/{contactId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List activities for a contact")
    public ResponseEntity<List<CrmActivity>> listByContact(@PathVariable Long contactId) {
        return ResponseEntity.ok(crmActivityService.listByContact(contactId));
    }

    @GetMapping("/opportunity/{opportunityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List activities for an opportunity")
    public ResponseEntity<List<CrmActivity>> listByOpportunity(@PathVariable Long opportunityId) {
        return ResponseEntity.ok(crmActivityService.listByOpportunity(opportunityId));
    }
}
