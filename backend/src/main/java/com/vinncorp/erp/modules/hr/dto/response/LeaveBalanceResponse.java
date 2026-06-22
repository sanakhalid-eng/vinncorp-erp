package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.HrLeaveBalance;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private Integer year;
    private BigDecimal totalDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal carriedOverDays;
    private BigDecimal availableDays;

    public static LeaveBalanceResponse from(HrLeaveBalance entity) {
        return LeaveBalanceResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .leaveTypeId(entity.getLeaveType() != null ? entity.getLeaveType().getId() : null)
                .leaveTypeName(entity.getLeaveType() != null ? entity.getLeaveType().getName() : null)
                .year(entity.getYear())
                .totalDays(entity.getTotalDays())
                .usedDays(entity.getUsedDays())
                .pendingDays(entity.getPendingDays())
                .carriedOverDays(entity.getCarriedOverDays())
                .availableDays(entity.getAvailableDays())
                .build();
    }
}
