package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.dto.response.EmployeeUtilizationResponse;
import com.vinncorp.erp.modules.hr.dto.response.UtilizationSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface UtilizationReportService {

    UtilizationSummaryResponse getUtilizationSummary(Long workspaceId, LocalDate startDate, LocalDate endDate);

    List<EmployeeUtilizationResponse> getEmployeeUtilization(Long workspaceId, LocalDate startDate, LocalDate endDate);

    EmployeeUtilizationResponse getEmployeeUtilizationById(Long employeeId, Long workspaceId, LocalDate startDate, LocalDate endDate);

    List<EmployeeUtilizationResponse> getDepartmentUtilization(Long departmentId, Long workspaceId, LocalDate startDate, LocalDate endDate);

    List<EmployeeUtilizationResponse> getProjectUtilization(Long projectId, Long workspaceId, LocalDate startDate, LocalDate endDate);

    byte[] exportUtilizationReport(Long workspaceId, LocalDate startDate, LocalDate endDate, String format);
}
