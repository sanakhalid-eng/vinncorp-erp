package com.vinncorp.erp.modules.finance.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.modules.finance.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_payments", indexes = {
    @Index(name = "idx_finance_payments_workspace", columnList = "workspace_id"),
    @Index(name = "idx_finance_payments_invoice", columnList = "workspace_id, invoice_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Payment extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "invoice_id", insertable = false, updatable = false)
    private Long invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "reference_number", length = 128)
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
