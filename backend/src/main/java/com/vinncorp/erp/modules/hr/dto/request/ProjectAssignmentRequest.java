package com.vinncorp.erp.modules.hr.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAssignmentRequest {

    @NotNull(message = "employeeId is required")
    private Long employeeId;

    @NotNull(message = "projectId is required")
    private Long projectId;

    private String projectName;

    private String roleInProject;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal allocationPercentage;

    private String notes;
}
