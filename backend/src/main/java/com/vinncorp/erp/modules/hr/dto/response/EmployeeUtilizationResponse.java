package com.vinncorp.erp.modules.hr.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUtilizationResponse {
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String department;
    private String designation;
    private Long userId;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private BigDecimal loggedHours;
    private BigDecimal billableHours;
    private BigDecimal expectedHours;
    private BigDecimal overtimeHours;
    private BigDecimal utilizationPercentage;
    private BigDecimal productivityScore;

    private long totalTasks;
    private long completedTasks;
    private long activeTasks;

    private int workingDays;
    private int attendanceDays;
    private BigDecimal attendanceRate;

    private String rating;
}
