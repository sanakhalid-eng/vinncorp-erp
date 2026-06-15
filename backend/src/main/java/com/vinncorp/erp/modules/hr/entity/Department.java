package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "hr_departments",
    uniqueConstraints = @UniqueConstraint(name = "uk_hr_departments_workspace_name", columnNames = {"workspace_id", "name"}),
    indexes = {
        @Index(name = "idx_hr_departments_workspace", columnList = "workspace_id"),
        @Index(name = "idx_hr_departments_head", columnList = "head_employee_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Department extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 32)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "head_employee_id")
    private Long headEmployeeId;

    @Column(name = "parent_department_id")
    private Long parentDepartmentId;

    @Column(nullable = false)
    private boolean active = true;
}

