package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.dto.request.LeaveBalanceSeedRequest;
import com.vinncorp.erp.modules.hr.dto.response.LeaveBalanceResponse;

import java.util.List;

public interface LeaveBalanceService {
    LeaveBalanceResponse getBalance(Long employeeId, Long leaveTypeId, Integer year, Long workspaceId);
    List<LeaveBalanceResponse> getBalancesByEmployee(Long employeeId, Integer year, Long workspaceId);
    List<LeaveBalanceResponse> getBalancesByWorkspace(Long workspaceId, Integer year);
    LeaveBalanceResponse seedBalance(LeaveBalanceSeedRequest request, Long workspaceId);
    void deductBalance(Long employeeId, Long leaveTypeId, Integer year, java.math.BigDecimal days, Long workspaceId);
    void restoreBalance(Long employeeId, Long leaveTypeId, Integer year, java.math.BigDecimal days, Long workspaceId);
    void pendingDeduction(Long employeeId, Long leaveTypeId, Integer year, java.math.BigDecimal days, Long workspaceId);
    void pendingRestore(Long employeeId, Long leaveTypeId, Integer year, java.math.BigDecimal days, Long workspaceId);
}
