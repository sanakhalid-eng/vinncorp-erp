package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.HrHoliday;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayResponse {
    private Long id;
    private String name;
    private LocalDate holidayDate;
    private String holidayType;
    private String description;
    private Boolean isRecurring;

    public static HolidayResponse from(HrHoliday entity) {
        return HolidayResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .holidayDate(entity.getHolidayDate())
                .holidayType(entity.getHolidayType())
                .description(entity.getDescription())
                .isRecurring(entity.getIsRecurring())
                .build();
    }
}
