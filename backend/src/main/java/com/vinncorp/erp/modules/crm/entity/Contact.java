package com.vinncorp.erp.modules.crm.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crm_contacts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_crm_contacts_workspace_email", columnNames = {"workspace_id", "email"})
}, indexes = {
    @Index(name = "idx_crm_contacts_workspace", columnList = "workspace_id"),
    @Index(name = "idx_crm_contacts_name", columnList = "workspace_id, last_name, first_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Contact extends BaseTenantEntity implements TenantScopedEntity {

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

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getFullName() {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
