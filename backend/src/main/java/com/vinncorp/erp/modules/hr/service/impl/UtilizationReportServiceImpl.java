package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.entity.HrAttendance;
import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.repository.HrAttendanceRepository;
import com.vinncorp.erp.modules.hr.dto.response.DepartmentUtilizationResponse;
import com.vinncorp.erp.modules.hr.dto.response.EmployeeUtilizationResponse;
import com.vinncorp.erp.modules.hr.dto.response.ProjectUtilizationResponse;
import com.vinncorp.erp.modules.hr.dto.response.UtilizationSummaryResponse;
import com.vinncorp.erp.modules.hr.service.UtilizationReportService;
import com.vinncorp.erp.modules.projects.entity.TimeLog;
import com.vinncorp.erp.modules.projects.repository.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilizationReportServiceImpl implements UtilizationReportService {

    private static final BigDecimal STANDARD_HOURS_PER_DAY = new BigDecimal("8");
    private static final BigDecimal BILLABLE_TARGET_PERCENTAGE = new BigDecimal("75");

    private final EmployeeRepository employeeRepository;
    private final HrAttendanceRepository attendanceRepository;
    private final TimeLogRepository timeLogRepository;

    @Override
    @Transactional(readOnly = true)
    public UtilizationSummaryResponse getUtilizationSummary(Long workspaceId, LocalDate startDate, LocalDate endDate) {
        List<Employee> employees = employeeRepository.findAllByWorkspaceIdAndStatus(workspaceId, EmployeeStatus.ACTIVE);
        List<EmployeeUtilizationResponse> allUtil = employees.stream()
                .map(e -> calculateEmployeeUtilization(e, workspaceId, startDate, endDate))
                .filter(Objects::nonNull)
                .toList();

        BigDecimal totalLogged = allUtil.stream()
                .map(EmployeeUtilizationResponse::getLoggedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOvertime = allUtil.stream()
                .map(EmployeeUtilizationResponse::getOvertimeHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgUtil = allUtil.stream()
                .map(EmployeeUtilizationResponse::getUtilizationPercentage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAtt = allUtil.stream()
                .map(EmployeeUtilizationResponse::getAttendanceRate)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTasks = allUtil.stream().mapToLong(EmployeeUtilizationResponse::getTotalTasks).sum();
        long completedTasks = allUtil.stream().mapToLong(EmployeeUtilizationResponse::getCompletedTasks).sum();

        List<EmployeeUtilizationResponse> topPerformers = allUtil.stream()
                .sorted(Comparator.comparing(EmployeeUtilizationResponse::getUtilizationPercentage).reversed())
                .limit(5)
                .toList();

        List<EmployeeUtilizationResponse> underUtilized = allUtil.stream()
                .filter(e -> e.getUtilizationPercentage() != null && e.getUtilizationPercentage().compareTo(new BigDecimal("50")) < 0)
                .sorted(Comparator.comparing(EmployeeUtilizationResponse::getUtilizationPercentage))
                .limit(5)
                .toList();

        List<DepartmentUtilizationResponse> byDepartment = calculateDepartmentUtilization(allUtil);
        List<ProjectUtilizationResponse> byProject = calculateProjectUtilization(workspaceId, startDate, endDate);

        int activeCount = (int) allUtil.stream()
                .filter(e -> e.getLoggedHours() != null && e.getLoggedHours().compareTo(BigDecimal.ZERO) > 0)
                .count();

        return UtilizationSummaryResponse.builder()
                .periodStart(startDate)
                .periodEnd(endDate)
                .totalEmployees(employees.size())
                .activeEmployees(employees.size())
                .employeesWithData(activeCount)
                .averageUtilization(activeCount > 0 ? avgUtil.divide(BigDecimal.valueOf(activeCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .averageAttendanceRate(activeCount > 0 ? avgAtt.divide(BigDecimal.valueOf(activeCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .totalLoggedHours(totalLogged)
                .totalOvertimeHours(totalOvertime)
                .totalTasksAssigned(totalTasks)
                .totalTasksCompleted(completedTasks)
                .topPerformers(topPerformers)
                .underUtilized(underUtilized)
                .byDepartment(byDepartment)
                .byProject(byProject)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeUtilizationResponse> getEmployeeUtilization(Long workspaceId, LocalDate startDate, LocalDate endDate) {
        List<Employee> employees = employeeRepository.findAllByWorkspaceIdAndStatus(workspaceId, EmployeeStatus.ACTIVE);
        return employees.stream()
                .map(e -> calculateEmployeeUtilization(e, workspaceId, startDate, endDate))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EmployeeUtilizationResponse::getUtilizationPercentage).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeUtilizationResponse getEmployeeUtilizationById(Long employeeId, Long workspaceId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.findByIdAndWorkspaceId(employeeId, workspaceId)
                .orElse(null);
        if (employee == null) return null;
        return calculateEmployeeUtilization(employee, workspaceId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeUtilizationResponse> getDepartmentUtilization(Long departmentId, Long workspaceId, LocalDate startDate, LocalDate endDate) {
        List<Employee> employees = employeeRepository.findAllByWorkspaceIdAndStatus(workspaceId, EmployeeStatus.ACTIVE);
        return employees.stream()
                .filter(e -> departmentId.equals(e.getDepartmentId()))
                .map(e -> calculateEmployeeUtilization(e, workspaceId, startDate, endDate))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EmployeeUtilizationResponse::getUtilizationPercentage).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeUtilizationResponse> getProjectUtilization(Long projectId, Long workspaceId, LocalDate startDate, LocalDate endDate) {
        List<TimeLog> projectLogs = timeLogRepository.findByProjectIdAndWorkspaceIdAndDateRange(projectId, workspaceId, startDate, endDate);
        Set<Long> userIds = projectLogs.stream()
                .map(tl -> tl.getUser().getId())
                .collect(Collectors.toSet());

        List<EmployeeUtilizationResponse> results = new ArrayList<>();
        for (Long userId : userIds) {
            Employee emp = employeeRepository.findByUserIdAndWorkspaceId(userId, workspaceId).orElse(null);
            if (emp != null) {
                EmployeeUtilizationResponse util = calculateEmployeeUtilization(emp, workspaceId, startDate, endDate);
                if (util != null) {
                    BigDecimal projectHours = projectLogs.stream()
                            .filter(tl -> tl.getUser().getId().equals(userId))
                            .map(TimeLog::getHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    util.setLoggedHours(projectHours);
                    results.add(util);
                }
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportUtilizationReport(Long workspaceId, LocalDate startDate, LocalDate endDate, String format) {
        List<EmployeeUtilizationResponse> data = getEmployeeUtilization(workspaceId, startDate, endDate);

        if ("csv".equalsIgnoreCase(format)) {
            return exportAsCsv(data, startDate, endDate);
        }
        return exportAsCsv(data, startDate, endDate);
    }

    private EmployeeUtilizationResponse calculateEmployeeUtilization(Employee employee, Long workspaceId,
                                                                      LocalDate startDate, LocalDate endDate) {
        Long userId = employee.getUserId();
        if (userId == null) return null;

        BigDecimal loggedHours = timeLogRepository.getTotalHoursByUserIdAndDateRangeAndWorkspace(userId, workspaceId, startDate, endDate);
        if (loggedHours == null) loggedHours = BigDecimal.ZERO;

        int workingDays = countWorkingDays(startDate, endDate);
        BigDecimal expectedHours = STANDARD_HOURS_PER_DAY.multiply(BigDecimal.valueOf(workingDays));

        List<HrAttendance> attendance = attendanceRepository.findByEmployeeIdAndAttendanceDateBetweenAndWorkspaceId(
                employee.getId(), startDate, endDate, workspaceId);
        long attendanceDays = attendance.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                .count();
        BigDecimal totalWorkHours = attendance.stream()
                .map(HrAttendance::getWorkHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overtimeHours = attendance.stream()
                .map(HrAttendance::getOvertimeHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal utilization = expectedHours.compareTo(BigDecimal.ZERO) > 0
                ? loggedHours.multiply(BigDecimal.valueOf(100)).divide(expectedHours, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal attendanceRate = workingDays > 0
                ? BigDecimal.valueOf(attendanceDays).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String rating = determineRating(utilization);

        return EmployeeUtilizationResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .userId(userId)
                .periodStart(startDate)
                .periodEnd(endDate)
                .loggedHours(loggedHours)
                .billableHours(loggedHours)
                .expectedHours(expectedHours)
                .overtimeHours(overtimeHours)
                .utilizationPercentage(utilization)
                .productivityScore(utilization)
                .workingDays(workingDays)
                .attendanceDays((int) attendanceDays)
                .attendanceRate(attendanceRate)
                .rating(rating)
                .build();
    }

    private int countWorkingDays(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    private String determineRating(BigDecimal utilization) {
        if (utilization == null) return "N/A";
        if (utilization.compareTo(new BigDecimal("90")) >= 0) return "Excellent";
        if (utilization.compareTo(new BigDecimal("75")) >= 0) return "Good";
        if (utilization.compareTo(new BigDecimal("50")) >= 0) return "Average";
        if (utilization.compareTo(new BigDecimal("25")) >= 0) return "Below Average";
        return "Poor";
    }

    private List<DepartmentUtilizationResponse> calculateDepartmentUtilization(List<EmployeeUtilizationResponse> allUtil) {
        Map<String, List<EmployeeUtilizationResponse>> byDept = allUtil.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(EmployeeUtilizationResponse::getDepartment));

        return byDept.entrySet().stream()
                .map(entry -> {
                    List<EmployeeUtilizationResponse> deptEmployees = entry.getValue();
                    BigDecimal avgUtil = deptEmployees.stream()
                            .map(EmployeeUtilizationResponse::getUtilizationPercentage)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalHours = deptEmployees.stream()
                            .map(EmployeeUtilizationResponse::getLoggedHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgAtt = deptEmployees.stream()
                            .map(EmployeeUtilizationResponse::getAttendanceRate)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int size = deptEmployees.size();
                    return DepartmentUtilizationResponse.builder()
                            .departmentName(entry.getKey())
                            .employeeCount(size)
                            .averageUtilization(size > 0 ? avgUtil.divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                            .totalLoggedHours(totalHours)
                            .averageAttendanceRate(size > 0 ? avgAtt.divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                            .build();
                })
                .sorted(Comparator.comparing(DepartmentUtilizationResponse::getAverageUtilization).reversed())
                .toList();
    }

    private List<ProjectUtilizationResponse> calculateProjectUtilization(Long workspaceId, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    private byte[] exportAsCsv(List<EmployeeUtilizationResponse> data, LocalDate startDate, LocalDate endDate) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Employee Utilization Report");
        writer.println("Period: " + startDate + " to " + endDate);
        writer.println();
        writer.println("Employee Code,Employee Name,Department,Logged Hours,Expected Hours,Overtime Hours,Utilization %,Attendance Rate,Rating");

        for (EmployeeUtilizationResponse emp : data) {
            writer.println(String.join(",",
                    safeStr(emp.getEmployeeCode()),
                    safeStr(emp.getEmployeeName()),
                    safeStr(emp.getDepartment()),
                    str(emp.getLoggedHours()),
                    str(emp.getExpectedHours()),
                    str(emp.getOvertimeHours()),
                    str(emp.getUtilizationPercentage()),
                    str(emp.getAttendanceRate()),
                    safeStr(emp.getRating())
            ));
        }

        writer.flush();
        return out.toByteArray();
    }

    private String safeStr(String val) {
        return val != null ? "\"" + val.replace("\"", "\"\"") + "\"" : "";
    }

    private String str(BigDecimal val) {
        return val != null ? val.toPlainString() : "0";
    }
}
