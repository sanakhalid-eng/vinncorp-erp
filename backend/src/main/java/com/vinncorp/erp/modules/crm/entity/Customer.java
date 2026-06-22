package com.vinncorp.erp.modules.crm.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "crm_customers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_crm_customers_workspace_name", columnNames = {"workspace_id", "name"})
}, indexes = {
    @Index(name = "idx_crm_customers_workspace", columnList = "workspace_id"),
    @Index(name = "idx_crm_customers_owner", columnList = "contact_owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Customer extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(length = 500)
    private String website;

    @Column(length = 32)
    private String phone;

    @Column(length = 200)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "contact_owner_id")
    private Long contactOwnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_owner_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User contactOwner;
}
