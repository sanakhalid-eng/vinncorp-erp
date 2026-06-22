package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;
import com.vinncorp.erp.modules.hr.enums.LeaveRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_leave_requests", indexes = {
    @Index(name = "idx_hr_leave_requests_workspace", columnList = "workspace_id"),
    @Index(name = "idx_hr_leave_requests_employee", columnList = "employee_id"),
    @Index(name = "idx_hr_leave_requests_type", columnList = "leave_type_id"),
    @Index(name = "idx_hr_leave_requests_status", columnList = "workspace_id, status"),
    @Index(name = "idx_hr_leave_requests_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_hr_leave_requests_pending", columnList = "workspace_id, status, start_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLRestriction("deleted_at IS NULL")
public class HrLeaveRequest extends BaseTenantEntity {

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

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal totalDays;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "request_year", nullable = false)
    private Integer year;
}
