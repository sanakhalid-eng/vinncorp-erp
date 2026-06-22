package com.vinncorp.erp.modules.hr.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentUtilizationResponse {
    private Long departmentId;
    private String departmentName;
    private int employeeCount;
    private BigDecimal averageUtilization;
    private BigDecimal totalLoggedHours;
    private BigDecimal averageAttendanceRate;
}

