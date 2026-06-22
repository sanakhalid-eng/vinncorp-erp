package com.vinncorp.erp.modules.hr.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUtilizationResponse {
    private Long projectId;
    private String projectName;
    private int memberCount;
    private BigDecimal totalHours;
    private BigDecimal averageHoursPerMember;
}
