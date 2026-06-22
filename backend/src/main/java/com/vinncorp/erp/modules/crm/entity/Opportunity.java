package com.vinncorp.erp.modules.crm.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crm_opportunities", indexes = {
    @Index(name = "idx_crm_opps_workspace", columnList = "workspace_id"),
    @Index(name = "idx_crm_opps_stage", columnList = "stage_id"),
    @Index(name = "idx_crm_opps_owner", columnList = "owner_id"),
    @Index(name = "idx_crm_opps_customer", columnList = "customer_id"),
    @Index(name = "idx_crm_opps_lead", columnList = "lead_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Opportunity extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(precision = 15, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    @Column(length = 3)
    private String currency = "USD";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private PipelineStage stage;

    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Customer customer;

    @Column(name = "lead_id")
    private Long leadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Lead lead;

    @Column(name = "owner_id")
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User owner;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "actual_close_date")
    private LocalDate actualCloseDate;

    @Column(name = "probability_pct")
    private int probabilityPct = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
