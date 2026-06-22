package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.HrShift;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftResponse {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private Integer gracePeriodMinutes;
    private Boolean isActive;

    public static ShiftResponse from(HrShift entity) {
        return ShiftResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .breakMinutes(entity.getBreakMinutes())
                .gracePeriodMinutes(entity.getGracePeriodMinutes())
                .isActive(entity.getIsActive())
                .build();
    }
}
