package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.HrAttendance;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status;
    private Long shiftId;
    private String shiftName;
    private BigDecimal workHours;
    private BigDecimal overtimeHours;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AttendanceResponse from(HrAttendance entity) {
        return AttendanceResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .employeeEmail(entity.getEmployee() != null ? entity.getEmployee().getWorkEmail() : null)
                .attendanceDate(entity.getAttendanceDate())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .status(entity.getStatus())
                .shiftId(entity.getShift() != null ? entity.getShift().getId() : null)
                .shiftName(entity.getShift() != null ? entity.getShift().getName() : null)
                .workHours(entity.getWorkHours())
                .overtimeHours(entity.getOvertimeHours())
                .lateMinutes(entity.getLateMinutes())
                .earlyLeaveMinutes(entity.getEarlyLeaveMinutes())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
