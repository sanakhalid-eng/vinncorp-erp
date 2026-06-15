package com.vinncorp.erp.modules.hr.response;

import com.vinncorp.erp.modules.hr.entity.HrProjectAssignment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAssignmentResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long projectId;
    private String projectName;
    private String roleInProject;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal allocationPercentage;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectAssignmentResponse from(HrProjectAssignment entity) {
        return ProjectAssignmentResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .employeeCode(entity.getEmployee() != null ? entity.getEmployee().getEmployeeCode() : null)
                .projectId(entity.getProjectId())
                .projectName(entity.getProjectName())
                .roleInProject(entity.getRoleInProject())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .allocationPercentage(entity.getAllocationPercentage())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
