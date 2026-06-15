package com.vinncorp.erp.modules.hr.response;

import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.enums.EmploymentType;
import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Long id;
    private Long workspaceId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String workEmail;
    private String personalEmail;
    private String phone;
    private EmploymentType employmentType;
    private EmployeeStatus status;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private String jobTitle;
    private String timezone;
    private String locale;
    private Long managerId;
    private Long userId;
    private Long departmentId;
    private Long designationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse from(Employee e) {
        if (e == null) return null;
        return EmployeeResponse.builder()
            .id(e.getId())
            .workspaceId(e.getWorkspaceId())
            .employeeCode(e.getEmployeeCode())
            .firstName(e.getFirstName())
            .lastName(e.getLastName())
            .fullName(e.getFullName())
            .workEmail(e.getWorkEmail())
            .personalEmail(e.getPersonalEmail())
            .phone(e.getPhone())
            .employmentType(e.getEmploymentType())
            .status(e.getStatus())
            .dateOfBirth(e.getDateOfBirth())
            .hireDate(e.getHireDate())
            .terminationDate(e.getTerminationDate())
            .jobTitle(e.getJobTitle())
            .timezone(e.getTimezone())
            .locale(e.getLocale())
            .managerId(e.getManagerId())
            .userId(e.getUserId())
            .departmentId(e.getDepartmentId())
            .designationId(e.getDesignationId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }
}

