package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.entity.HrAttendance;
import com.vinncorp.erp.modules.hr.entity.HrShift;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.repository.HrAttendanceRepository;
import com.vinncorp.erp.modules.hr.repository.HrShiftRepository;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceCheckInRequest;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceCheckOutRequest;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceUpdateRequest;
import com.vinncorp.erp.modules.hr.dto.response.AttendanceDashboardResponse;
import com.vinncorp.erp.modules.hr.dto.response.AttendanceResponse;
import com.vinncorp.erp.modules.hr.service.AttendanceService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final HrAttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final HrShiftRepository shiftRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AttendanceResponse checkIn(AttendanceCheckInRequest request, Long workspaceId, String ip) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (attendanceRepository.existsByEmployeeIdAndAttendanceDateAndWorkspaceId(
                request.getEmployeeId(), request.getAttendanceDate(), workspaceId)) {
            throw new ConflictException("Attendance already recorded for this date");
        }

        HrAttendance attendance = new HrAttendance();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(LocalDateTime.of(request.getAttendanceDate(), LocalTime.now()));
        attendance.setCheckInIp(ip);

        if (request.getShiftId() != null) {
            HrShift shift = shiftRepository.findByIdAndWorkspaceId(request.getShiftId(), workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
            attendance.setShift(shift);
            attendance.setStatus("PRESENT");

            LocalDateTime shiftStart = LocalDateTime.of(request.getAttendanceDate(), shift.getStartTime());
            if (attendance.getCheckInTime().isAfter(shiftStart.plusMinutes(shift.getGracePeriodMinutes()))) {
                long lateMins = Duration.between(shiftStart, attendance.getCheckInTime()).toMinutes();
                attendance.setLateMinutes((int) lateMins);
            }
        } else {
            attendance.setStatus("PRESENT");
        }

        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        attendance.setWorkspace(workspace);

        HrAttendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.from(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(AttendanceCheckOutRequest request, Long workspaceId, String ip) {
        HrAttendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDateAndWorkspaceId(
                        request.getEmployeeId(), request.getAttendanceDate(), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("No check-in found for this date"));

        if (attendance.getCheckOutTime() != null) {
            throw new BadRequestException("Already checked out for this date");
        }

        attendance.setCheckOutTime(LocalDateTime.of(request.getAttendanceDate(), LocalTime.now()));
        attendance.setCheckOutIp(ip);

        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        if (attendance.getCheckInTime() != null) {
            Duration workDuration = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
            BigDecimal hours = BigDecimal.valueOf(workDuration.toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            attendance.setWorkHours(hours);

            if (attendance.getShift() != null) {
                Duration shiftDuration = Duration.between(
                        attendance.getShift().getStartTime(),
                        attendance.getShift().getEndTime());
                long shiftMinutes = shiftDuration.toMinutes() - attendance.getShift().getBreakMinutes();
                BigDecimal shiftHours = BigDecimal.valueOf(shiftMinutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                if (hours.compareTo(shiftHours) > 0) {
                    attendance.setOvertimeHours(hours.subtract(shiftHours));
                }

                LocalDateTime expectedEnd = LocalDateTime.of(
                        attendance.getAttendanceDate(),
                        attendance.getShift().getEndTime());
                if (attendance.getCheckOutTime().isBefore(expectedEnd)) {
                    long earlyMins = Duration.between(attendance.getCheckOutTime(), expectedEnd).toMinutes();
                    attendance.setEarlyLeaveMinutes((int) earlyMins);
                }
            }
        }

        HrAttendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.from(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceUpdateRequest request, Long workspaceId) {
        HrAttendance attendance = attendanceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));

        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getCheckInTime() != null) attendance.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutTime() != null) attendance.setCheckOutTime(request.getCheckOutTime());
        if (request.getWorkHours() != null) attendance.setWorkHours(request.getWorkHours());
        if (request.getOvertimeHours() != null) attendance.setOvertimeHours(request.getOvertimeHours());
        if (request.getLateMinutes() != null) attendance.setLateMinutes(request.getLateMinutes());
        if (request.getEarlyLeaveMinutes() != null) attendance.setEarlyLeaveMinutes(request.getEarlyLeaveMinutes());
        if (request.getNotes() != null) attendance.setNotes(request.getNotes());
        if (request.getShiftId() != null) {
            HrShift shift = shiftRepository.findByIdAndWorkspaceId(request.getShiftId(), workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
            attendance.setShift(shift);
        }

        HrAttendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.from(saved);
    }

    @Override
    public AttendanceResponse getAttendance(Long id, Long workspaceId) {
        HrAttendance attendance = attendanceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        return AttendanceResponse.from(attendance);
    }

    @Override
    public List<AttendanceResponse> getEmployeeAttendance(Long employeeId, Long workspaceId,
                                                           LocalDate startDate, LocalDate endDate) {
        return attendanceRepository
                .findByEmployeeIdAndAttendanceDateBetweenAndWorkspaceId(employeeId, startDate, endDate, workspaceId)
                .stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date, Long workspaceId) {
        return attendanceRepository.findByAttendanceDateAndWorkspaceId(date, workspaceId)
                .stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceDashboardResponse getDashboard(LocalDate date, Long workspaceId) {
        long totalEmployees = employeeRepository.count();
        long presentCount = attendanceRepository.countByWorkspaceIdAndDateAndStatus(workspaceId, date, "PRESENT");
        long lateCount = attendanceRepository.countByWorkspaceIdAndDateAndStatus(workspaceId, date, "LATE");
        long halfDayCount = attendanceRepository.countByWorkspaceIdAndDateAndStatus(workspaceId, date, "HALF_DAY");
        long onLeaveCount = attendanceRepository.countByWorkspaceIdAndDateAndStatus(workspaceId, date, "ON_LEAVE");
        long totalMarked = attendanceRepository.countByWorkspaceIdAndDate(workspaceId, date);
        long absentCount = totalEmployees - totalMarked;

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        List<Object[]> grouped = attendanceRepository.countByStatusGrouped(workspaceId, date);
        for (Object[] row : grouped) {
            statusBreakdown.put((String) row[0], (Long) row[1]);
        }

        double attendancePercentage = totalEmployees > 0
                ? (double) (presentCount + lateCount + halfDayCount) / totalEmployees * 100
                : 0;

        return AttendanceDashboardResponse.builder()
                .date(date)
                .totalEmployees(totalEmployees)
                .presentCount(presentCount)
                .absentCount(Math.max(0, absentCount))
                .lateCount(lateCount)
                .onLeaveCount(onLeaveCount)
                .halfDayCount(halfDayCount)
                .statusBreakdown(statusBreakdown)
                .attendancePercentage(Math.round(attendancePercentage * 100.0) / 100.0)
                .build();
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id, Long workspaceId) {
        HrAttendance attendance = attendanceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        User user = userRepository.findByEmail("system").orElse(null);
        Long userId = user != null ? user.getId() : null;
        attendance.softDelete(userId);
        attendanceRepository.save(attendance);
    }
}
