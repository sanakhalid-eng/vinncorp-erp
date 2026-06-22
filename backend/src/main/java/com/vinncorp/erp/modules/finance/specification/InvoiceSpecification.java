package com.vinncorp.erp.modules.finance.specification;

import com.vinncorp.erp.modules.finance.entity.Invoice;
import com.vinncorp.erp.modules.finance.enums.InvoiceStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecification {

    public static Specification<Invoice> withFilters(Long workspaceId, String search, InvoiceStatus status,
                                                      Long customerId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("workspaceId"), workspaceId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), dateTo));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("invoiceNumber")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Invoice> overdueInvoices(Long workspaceId) {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("workspaceId"), workspaceId),
            cb.isNull(root.get("deletedAt")),
            root.get("status").in(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID),
            cb.lessThan(root.get("dueDate"), LocalDateTime.now())
        );
    }

    public static Specification<Invoice> outstandingInvoices(Long workspaceId) {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("workspaceId"), workspaceId),
            cb.isNull(root.get("deletedAt")),
            root.get("status").in(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE)
        );
    }
}
