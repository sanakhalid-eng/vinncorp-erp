package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "hr_designations",
    uniqueConstraints = @UniqueConstraint(name = "uk_hr_designations_workspace_title", columnNames = {"workspace_id", "title"}),
    indexes = {
        @Index(name = "idx_hr_designations_workspace", columnList = "workspace_id"),
        @Index(name = "idx_hr_designations_level", columnList = "workspace_id, level")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Designation extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 32)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer level = 0;

    @Column(nullable = false)
    private boolean active = true;
}

