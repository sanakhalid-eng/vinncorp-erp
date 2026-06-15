package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "hr_project_assignments", indexes = {
    @Index(name = "idx_hr_project_assignments_workspace", columnList = "workspace_id"),
    @Index(name = "idx_hr_project_assignments_employee", columnList = "employee_id"),
    @Index(name = "idx_hr_project_assignments_project", columnList = "project_id"),
    @Index(name = "idx_hr_project_assignments_status", columnList = "workspace_id, status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_hr_project_assignments_emp_proj", columnNames = {"employee_id", "project_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLRestriction("deleted_at IS NULL")
public class HrProjectAssignment extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "project_name", length = 200)
    private String projectName;

    @Column(name = "role_in_project", length = 100)
    private String roleInProject;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "allocation_percentage", precision = 5, scale = 2)
    private java.math.BigDecimal allocationPercentage;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(columnDefinition = "TEXT")
    private String notes;
}
