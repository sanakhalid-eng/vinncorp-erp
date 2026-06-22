package com.vinncorp.erp.modules.hr.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeCreateRequest {
    @NotBlank(message = "Leave type name is required")
    private String name;

    @NotBlank(message = "Leave type code is required")
    private String code;

    private String description;

    @NotNull(message = "Default days is required")
    @Min(value = 0, message = "Default days must be at least 0")
    private Integer defaultDays;

    @Builder.Default
    private Boolean isPaid = true;
}
