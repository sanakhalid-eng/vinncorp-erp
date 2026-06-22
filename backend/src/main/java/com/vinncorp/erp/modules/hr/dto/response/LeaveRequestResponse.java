package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.HrLeaveRequest;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;
    private String status;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private LocalDateTime createdAt;

    public static LeaveRequestResponse from(HrLeaveRequest entity) {
        return LeaveRequestResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .employeeEmail(entity.getEmployee() != null ? entity.getEmployee().getWorkEmail() : null)
                .leaveTypeId(entity.getLeaveType() != null ? entity.getLeaveType().getId() : null)
                .leaveTypeName(entity.getLeaveType() != null ? entity.getLeaveType().getName() : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .totalDays(entity.getTotalDays())
                .reason(entity.getReason())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .rejectionReason(entity.getRejectionReason())
                .cancelledAt(entity.getCancelledAt())
                .cancelledBy(entity.getCancelledBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
