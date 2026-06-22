package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.HrLeaveType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer defaultDays;
    private Boolean isPaid;
    private Boolean isActive;

    public static LeaveTypeResponse from(HrLeaveType entity) {
        return LeaveTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .defaultDays(entity.getDefaultDays())
                .isPaid(entity.getIsPaid())
                .isActive(entity.getIsActive())
                .build();
    }
}
