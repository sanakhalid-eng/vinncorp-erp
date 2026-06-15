package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.dto.CustomerSummary;
import com.vinncorp.erp.modules.crm.entity.Contact;
import com.vinncorp.erp.modules.crm.entity.Customer;
import com.vinncorp.erp.modules.crm.repository.CrmActivityRepository;
import com.vinncorp.erp.modules.crm.repository.OpportunityRepository;
import com.vinncorp.erp.modules.crm.service.CustomerService;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm/customers")
@RequiredArgsConstructor
@Tag(name = "CRM Customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CurrentWorkspaceResolver workspaceResolver;
    private final OpportunityRepository opportunityRepository;
    private final CrmActivityRepository crmActivityRepository;
    private final ProjectRepository projectRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CUSTOMER_CREATE')")
    @Operation(summary = "Create customer")
    public ResponseEntity<Customer> create(@RequestBody Customer customer, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.create(customer, wsId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customers")
    public ResponseEntity<List<Customer>> list(@RequestParam(required = false) String search) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(customerService.search(wsId, search));
        }
        return ResponseEntity.ok(customerService.list(wsId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer")
    public ResponseEntity<Customer> get(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(customerService.get(id, wsId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "Update customer")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer customer, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(customerService.update(id, customer, wsId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CUSTOMER_DELETE')")
    @Operation(summary = "Delete customer")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        customerService.delete(id, wsId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/contacts")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "Add contact to customer")
    public ResponseEntity<Void> addContact(@PathVariable Long customerId, @RequestBody Map<String, Object> body) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        Long contactId = Long.valueOf(body.get("contactId").toString());
        boolean isPrimary = Boolean.TRUE.equals(body.get("isPrimary"));
        customerService.addContact(customerId, contactId, isPrimary, wsId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{customerId}/contacts/{contactId}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "Remove contact from customer")
    public ResponseEntity<Void> removeContact(@PathVariable Long customerId, @PathVariable Long contactId) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        customerService.removeContact(customerId, contactId, wsId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{customerId}/contacts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer contacts")
    public ResponseEntity<List<Contact>> getContacts(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getContacts(customerId));
    }

    @GetMapping("/{id}/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Customer 360 summary with aggregate stats")
    public ResponseEntity<CustomerSummary> getSummary(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        Customer customer = customerService.get(id, wsId);

        BigDecimal totalRevenue = opportunityRepository.sumWonValueByCustomerId(id, wsId);
        long wonDeals = opportunityRepository.countByCustomerIdAndWorkspaceIdAndStageIsWon(id, wsId, true);
        long openOpps = opportunityRepository.countOpenByCustomerId(id, wsId);
        long recentActivities = crmActivityRepository.countByCustomerIdAndWorkspaceId(id, wsId);

        // Count projects created from won deals for this customer
        long activeProjects = projectRepository.findByWorkspaceIdAndCategoryAndDeletedAtIsNull(wsId, "CRM Conversion")
                .stream()
                .filter(p -> p.getName() != null && p.isActive())
                .count();
        long closedProjects = projectRepository.findByWorkspaceIdAndCategoryAndDeletedAtIsNull(wsId, "CRM Conversion")
                .stream()
                .filter(p -> p.getName() != null && !p.isActive())
                .count();

        return ResponseEntity.ok(CustomerSummary.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .wonDeals(wonDeals)
                .openOpportunities(openOpps)
                .activeProjects(activeProjects)
                .closedProjects(closedProjects)
                .recentActivities(recentActivities)
                .build());
    }
}
