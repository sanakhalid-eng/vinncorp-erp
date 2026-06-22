package com.vinncorp.erp.modules.hr.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceUpdateRequest {
    private String status;
    private java.time.LocalDateTime checkInTime;
    private java.time.LocalDateTime checkOutTime;
    private java.math.BigDecimal workHours;
    private java.math.BigDecimal overtimeHours;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private String notes;
    private Long shiftId;
}
