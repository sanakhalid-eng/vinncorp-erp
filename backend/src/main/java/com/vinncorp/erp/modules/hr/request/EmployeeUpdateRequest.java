package com.vinncorp.erp.modules.hr.request;

import com.vinncorp.erp.modules.hr.enums.EmploymentType;
import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    @Size(max = 200)
    private String workEmail;

    @Email
    @Size(max = 200)
    private String personalEmail;

    @Size(max = 32)
    private String phone;

    private EmploymentType employmentType;

    private EmployeeStatus status;

    @Past
    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    private LocalDate terminationDate;

    @Size(max = 150)
    private String jobTitle;

    @Size(max = 16)
    private String timezone;

    @Size(max = 8)
    private String locale;

    private Long managerId;

    private Long userId;

    private Long departmentId;

    private Long designationId;
}
