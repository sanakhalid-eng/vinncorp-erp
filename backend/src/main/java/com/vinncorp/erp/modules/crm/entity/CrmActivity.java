package com.vinncorp.erp.modules.crm.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.modules.crm.enums.ActivityType;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crm_activities", indexes = {
    @Index(name = "idx_crm_activities_workspace", columnList = "workspace_id"),
    @Index(name = "idx_crm_activities_lead", columnList = "lead_id"),
    @Index(name = "idx_crm_activities_customer", columnList = "customer_id"),
    @Index(name = "idx_crm_activities_contact", columnList = "contact_id"),
    @Index(name = "idx_crm_activities_opportunity", columnList = "opportunity_id"),
    @Index(name = "idx_crm_activities_date", columnList = "activity_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CrmActivity extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityType type;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_date", nullable = false)
    private LocalDateTime activityDate = LocalDateTime.now();

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "opportunity_id")
    private Long opportunityId;

}
