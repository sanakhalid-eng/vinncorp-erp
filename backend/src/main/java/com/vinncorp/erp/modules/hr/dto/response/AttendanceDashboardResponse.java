package com.vinncorp.erp.modules.hr.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDashboardResponse {
    private LocalDate date;
    private long totalEmployees;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private long onLeaveCount;
    private long halfDayCount;
    private Map<String, Long> statusBreakdown;
    private double attendancePercentage;
}
