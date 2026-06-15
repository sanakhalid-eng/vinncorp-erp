package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_leave_balances", indexes = {
    @Index(name = "idx_hr_leave_balances_workspace", columnList = "workspace_id"),
    @Index(name = "idx_hr_leave_balances_employee", columnList = "employee_id"),
    @Index(name = "idx_hr_leave_balances_type", columnList = "leave_type_id"),
    @Index(name = "idx_hr_leave_balances_year", columnList = "employee_id, request_year")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_hr_leave_balances_emp_type_year", columnNames = {"employee_id", "leave_type_id", "request_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLRestriction("deleted_at IS NULL")
public class HrLeaveBalance extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private HrLeaveType leaveType;

    @Column(name = "request_year", nullable = false)
    private Integer year;

    @Column(name = "total_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal totalDays = BigDecimal.ZERO;

    @Column(name = "used_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal usedDays = BigDecimal.ZERO;

    @Column(name = "pending_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal pendingDays = BigDecimal.ZERO;

    @Column(name = "carried_over_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal carriedOverDays = BigDecimal.ZERO;

    public BigDecimal getAvailableDays() {
        return totalDays.add(carriedOverDays).subtract(usedDays).subtract(pendingDays);
    }
}
