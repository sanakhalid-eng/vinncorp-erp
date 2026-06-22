package com.vinncorp.erp.modules.hr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 32)
    private String code;

    @Size(max = 500)
    private String description;

    private Integer level;

    private Boolean active;
}
