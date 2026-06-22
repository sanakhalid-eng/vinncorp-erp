package com.vinncorp.erp.modules.hr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftCreateRequest {
    @NotBlank(message = "Shift name is required")
    private String name;

    @NotNull(message = "Start time is required")
    private java.time.LocalTime startTime;

    @NotNull(message = "End time is required")
    private java.time.LocalTime endTime;

    @Builder.Default
    private Integer breakMinutes = 0;
    @Builder.Default
    private Integer gracePeriodMinutes = 0;
}
