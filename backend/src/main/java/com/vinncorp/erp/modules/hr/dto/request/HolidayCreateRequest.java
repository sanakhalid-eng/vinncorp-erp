package com.vinncorp.erp.modules.hr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayCreateRequest {
    @NotBlank(message = "Holiday name is required")
    private String name;

    @NotNull(message = "Holiday date is required")
    private java.time.LocalDate holidayDate;

    @Builder.Default
    private String holidayType = "PUBLIC";
    private String description;
    @Builder.Default
    private Boolean isRecurring = false;
}
