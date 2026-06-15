package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import com.vinncorp.erp.modules.hr.response.EmployeeResponse;

import java.util.List;
import java.util.Optional;

public interface HrPublicService {

    Optional<EmployeeResponse> getEmployeeByUserId(Long userId, Long workspaceId);

    Optional<EmployeeResponse> getEmployeeById(Long employeeId, Long workspaceId);

    List<EmployeeResponse> getActiveEmployees(Long workspaceId);

    List<EmployeeResponse> getEmployeesByStatus(Long workspaceId, EmployeeStatus status);

    boolean isEmployeeActive(Long employeeId, Long workspaceId);

    void handleEmployeeStatusChange(Long employeeId, EmployeeStatus oldStatus, EmployeeStatus newStatus, Long workspaceId);
}
