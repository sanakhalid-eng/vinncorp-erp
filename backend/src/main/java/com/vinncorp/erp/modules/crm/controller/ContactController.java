package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.entity.Contact;
import com.vinncorp.erp.modules.crm.service.ContactService;
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
@RequestMapping("/api/crm/contacts")
@RequiredArgsConstructor
@Tag(name = "CRM Contacts")
public class ContactController {

    private final ContactService contactService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CONTACT_CREATE')")
    @Operation(summary = "Create contact")
    public ResponseEntity<Contact> create(@RequestBody Contact contact, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactService.create(contact, wsId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List contacts")
    public ResponseEntity<List<Contact>> list(@RequestParam(required = false) String search) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(contactService.search(wsId, search));
        }
        return ResponseEntity.ok(contactService.list(wsId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get contact")
    public ResponseEntity<Contact> get(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(contactService.get(id, wsId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CONTACT_UPDATE')")
    @Operation(summary = "Update contact")
    public ResponseEntity<Contact> update(@PathVariable Long id, @RequestBody Contact contact, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(contactService.update(id, contact, wsId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CONTACT_DELETE')")
    @Operation(summary = "Delete contact")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        contactService.delete(id, wsId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
