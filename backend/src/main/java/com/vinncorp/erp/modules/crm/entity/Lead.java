package com.vinncorp.erp.modules.crm.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.crm.enums.LeadSource;
import com.vinncorp.erp.modules.crm.enums.LeadStatus;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "crm_leads", indexes = {
    @Index(name = "idx_crm_leads_workspace", columnList = "workspace_id"),
    @Index(name = "idx_crm_leads_status", columnList = "workspace_id, status"),
    @Index(name = "idx_crm_leads_owner", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Lead extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 200)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(length = 200)
    private String company;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LeadSource source = LeadSource.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadStatus status = LeadStatus.NEW;

    @Column(name = "owner_id")
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User owner;

    @Column(name = "converted_customer_id")
    private Long convertedCustomerId;

    @Column(name = "converted_by")
    private Long convertedBy;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getFullName() {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
