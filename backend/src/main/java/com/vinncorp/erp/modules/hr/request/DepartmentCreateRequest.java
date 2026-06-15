package com.vinncorp.erp.modules.hr.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 32)
    private String code;

    @Size(max = 500)
    private String description;

    private Long headEmployeeId;

    private Long parentDepartmentId;

    private Boolean active;
}
