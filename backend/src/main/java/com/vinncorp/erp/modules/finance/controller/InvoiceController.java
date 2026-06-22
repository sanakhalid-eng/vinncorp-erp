package com.vinncorp.erp.modules.finance.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.finance.dto.request.InvoiceCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.InvoiceUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.InvoiceResponse;
import com.vinncorp.erp.modules.finance.enums.InvoiceStatus;
import com.vinncorp.erp.modules.finance.service.InvoiceService;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/finance/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_CREATE')")
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceCreateRequest request,
                                                   Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.create(request, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<InvoiceResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Sort sortObj = Sort.by(Sort.Direction.fromString(dir), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(invoiceService.getList(workspaceId, search, status, customerId,
                dateFrom, dateTo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvoiceResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_EDIT')")
    public ResponseEntity<InvoiceResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody InvoiceUpdateRequest request,
                                                   Authentication auth) {
        return ResponseEntity.ok(invoiceService.update(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        invoiceService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_EDIT')")
    public ResponseEntity<InvoiceResponse> send(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(invoiceService.sendInvoice(id, auth.getName()));
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_MANAGE_PAYMENTS')")
    public ResponseEntity<InvoiceResponse> markPaid(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(invoiceService.markAsPaid(id, auth.getName()));
    }
}
