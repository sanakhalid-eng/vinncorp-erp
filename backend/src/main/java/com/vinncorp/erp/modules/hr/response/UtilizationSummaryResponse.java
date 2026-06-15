package com.vinncorp.erp.modules.hr.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilizationSummaryResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private int totalEmployees;
    private int activeEmployees;
    private int employeesWithData;

    private BigDecimal averageUtilization;
    private BigDecimal averageAttendanceRate;
    private BigDecimal totalLoggedHours;
    private BigDecimal totalOvertimeHours;

    private long totalTasksAssigned;
    private long totalTasksCompleted;

    private List<EmployeeUtilizationResponse> topPerformers;
    private List<EmployeeUtilizationResponse> underUtilized;
    private List<DepartmentUtilizationResponse> byDepartment;
    private List<ProjectUtilizationResponse> byProject;
}