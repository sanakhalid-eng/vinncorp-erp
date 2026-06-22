package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "hr_leave_types", indexes = {
    @Index(name = "idx_hr_leave_types_workspace", columnList = "workspace_id"),
    @Index(name = "idx_hr_leave_types_active", columnList = "workspace_id, is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_hr_leave_types_code_workspace", columnNames = {"workspace_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLRestriction("deleted_at IS NULL")
public class HrLeaveType extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_days", nullable = false)
    private Integer defaultDays = 0;

    @Column(name = "is_paid")
    private Boolean isPaid = true;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
