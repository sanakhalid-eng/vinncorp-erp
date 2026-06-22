package com.vinncorp.erp.modules.finance.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.modules.finance.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "finance_invoices", indexes = {
    @Index(name = "idx_finance_invoices_workspace", columnList = "workspace_id"),
    @Index(name = "idx_finance_invoices_status", columnList = "workspace_id, status"),
    @Index(name = "idx_finance_invoices_customer", columnList = "workspace_id, customer_id"),
    @Index(name = "idx_finance_invoices_due_date", columnList = "workspace_id, due_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Invoice extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "invoice_number", nullable = false, length = 64)
    private String invoiceNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "opportunity_id")
    private Long opportunityId;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_due", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceDue = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
}
