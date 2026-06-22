package com.vinncorp.erp.modules.crm.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crm_pipelines", uniqueConstraints = {
    @UniqueConstraint(name = "uk_crm_pipelines_workspace_name", columnNames = {"workspace_id", "name"})
}, indexes = {
    @Index(name = "idx_crm_pipelines_workspace", columnList = "workspace_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Pipeline extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
