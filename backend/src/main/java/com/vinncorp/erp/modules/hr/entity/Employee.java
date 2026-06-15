package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.hr.enums.EmploymentType;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "hr_employees",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_hr_employees_employee_code", columnNames = {"workspace_id", "employee_code"}),
        @UniqueConstraint(name = "uk_hr_employees_user", columnNames = {"workspace_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_hr_employees_workspace", columnList = "workspace_id"),
        @Index(name = "idx_hr_employees_department", columnList = "workspace_id, department_id"),
        @Index(name = "idx_hr_employees_designation", columnList = "workspace_id, designation_id"),
        @Index(name = "idx_hr_employees_status", columnList = "workspace_id, status"),
        @Index(name = "idx_hr_employees_email", columnList = "workspace_id, work_email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Employee extends BaseTenantEntity implements TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "employee_code", nullable = false, length = 32)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "work_email", length = 200)
    private String workEmail;

    @Column(name = "personal_email", length = 200)
    private String personalEmail;

    @Column(name = "phone", length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.vinncorp.erp.modules.hr.enums.EmployeeStatus status =
        com.vinncorp.erp.modules.hr.enums.EmployeeStatus.ACTIVE;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(length = 16)
    private String timezone;

    @Column(length = 8)
    private String locale;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "designation_id")
    private Long designationId;

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName);
        if (lastName != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(lastName);
        }
        return sb.toString();
    }
}

