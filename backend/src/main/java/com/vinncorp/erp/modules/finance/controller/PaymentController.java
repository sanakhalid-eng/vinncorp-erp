package com.vinncorp.erp.modules.finance.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.finance.dto.request.PaymentCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.PaymentUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.PaymentResponse;
import com.vinncorp.erp.modules.finance.service.PaymentService;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/finance/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_MANAGE_PAYMENTS')")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentCreateRequest request,
                                                   Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.create(request, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<PaymentResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Sort sortObj = Sort.by(Sort.Direction.fromString(dir), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(paymentService.getList(workspaceId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_MANAGE_PAYMENTS')")
    public ResponseEntity<PaymentResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody PaymentUpdateRequest request,
                                                   Authentication auth) {
        return ResponseEntity.ok(paymentService.update(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_MANAGE_PAYMENTS')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        paymentService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-invoice/{invoiceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentResponse>> getByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getByInvoiceId(invoiceId));
    }
}
