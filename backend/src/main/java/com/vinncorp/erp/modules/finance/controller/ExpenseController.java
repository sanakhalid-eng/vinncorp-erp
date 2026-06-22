package com.vinncorp.erp.modules.finance.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.finance.dto.request.ExpenseCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.ExpenseUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.ExpenseResponse;
import com.vinncorp.erp.modules.finance.enums.ExpenseStatus;
import com.vinncorp.erp.modules.finance.service.ExpenseService;
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
@RequestMapping("/api/finance/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_CREATE')")
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request,
                                                   Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.create(request, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<ExpenseResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expenseDate") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Sort sortObj = Sort.by(Sort.Direction.fromString(dir), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(expenseService.getList(workspaceId, search, status, category,
                dateFrom, dateTo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_EDIT')")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ExpenseUpdateRequest request,
                                                   Authentication auth) {
        return ResponseEntity.ok(expenseService.update(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        expenseService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_APPROVE_EXPENSE')")
    public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(expenseService.approve(id, auth.getName()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_APPROVE_EXPENSE')")
    public ResponseEntity<ExpenseResponse> reject(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(expenseService.reject(id, auth.getName()));
    }

    @PostMapping("/{id}/reimburse")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER') or hasAuthority('FINANCE_APPROVE_EXPENSE')")
    public ResponseEntity<ExpenseResponse> reimburse(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(expenseService.reimburse(id, auth.getName()));
    }
}
