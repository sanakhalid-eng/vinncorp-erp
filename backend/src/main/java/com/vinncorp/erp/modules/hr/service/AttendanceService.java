package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.dto.request.AttendanceCheckInRequest;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceCheckOutRequest;
import com.vinncorp.erp.modules.hr.dto.request.AttendanceUpdateRequest;
import com.vinncorp.erp.modules.hr.dto.response.AttendanceDashboardResponse;
import com.vinncorp.erp.modules.hr.dto.response.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceResponse checkIn(AttendanceCheckInRequest request, Long workspaceId, String ip);
    AttendanceResponse checkOut(AttendanceCheckOutRequest request, Long workspaceId, String ip);
    AttendanceResponse updateAttendance(Long id, AttendanceUpdateRequest request, Long workspaceId);
    AttendanceResponse getAttendance(Long id, Long workspaceId);
    List<AttendanceResponse> getEmployeeAttendance(Long employeeId, Long workspaceId, LocalDate startDate, LocalDate endDate);
    List<AttendanceResponse> getAttendanceByDate(LocalDate date, Long workspaceId);
    AttendanceDashboardResponse getDashboard(LocalDate date, Long workspaceId);
    void deleteAttendance(Long id, Long workspaceId);
}
