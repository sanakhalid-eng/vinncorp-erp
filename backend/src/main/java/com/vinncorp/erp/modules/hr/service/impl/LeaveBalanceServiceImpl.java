package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.entity.HrLeaveBalance;
import com.vinncorp.erp.modules.hr.entity.HrLeaveType;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.repository.HrLeaveBalanceRepository;
import com.vinncorp.erp.modules.hr.repository.HrLeaveTypeRepository;
import com.vinncorp.erp.modules.hr.request.LeaveBalanceSeedRequest;
import com.vinncorp.erp.modules.hr.response.LeaveBalanceResponse;
import com.vinncorp.erp.modules.hr.service.LeaveBalanceService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final HrLeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final HrLeaveTypeRepository leaveTypeRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(Long employeeId, Long leaveTypeId, Integer year, Long workspaceId) {
        HrLeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElse(null);
        if (balance == null) {
            return createZeroBalance(employeeId, leaveTypeId, year);
        }
        return toResponseSafe(balance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getBalancesByEmployee(Long employeeId, Integer year, Long workspaceId) {
        return leaveBalanceRepository.findByEmployeeIdAndYearOrderByCreatedAtDesc(employeeId, year)
                .stream().map(this::toResponseSafe).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getBalancesByWorkspace(Long workspaceId, Integer year) {
        return leaveBalanceRepository.findByWorkspaceIdAndYearOrderByEmployeeIdAsc(workspaceId, year)
                .stream().map(this::toResponseSafe).collect(Collectors.toList());
    }

    private LeaveBalanceResponse toResponseSafe(HrLeaveBalance entity) {
        String empName = "";
        String ltName = "";
        try {
            if (entity.getEmployee() != null) empName = entity.getEmployee().getFullName();
        } catch (Exception ignored) {}
        try {
            if (entity.getLeaveType() != null) ltName = entity.getLeaveType().getName();
        } catch (Exception ignored) {}
        return LeaveBalanceResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(empName)
                .leaveTypeId(entity.getLeaveType() != null ? entity.getLeaveType().getId() : null)
                .leaveTypeName(ltName)
                .year(entity.getYear())
                .totalDays(entity.getTotalDays())
                .usedDays(entity.getUsedDays())
                .pendingDays(entity.getPendingDays())
                .carriedOverDays(entity.getCarriedOverDays())
                .availableDays(entity.getAvailableDays())
                .build();
    }

    @Override
    @Transactional
    public LeaveBalanceResponse seedBalance(LeaveBalanceSeedRequest request, Long workspaceId) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        HrLeaveType leaveType = leaveTypeRepository.findByIdAndWorkspaceId(request.getLeaveTypeId(), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        HrLeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                request.getEmployeeId(), request.getLeaveTypeId(), request.getYear()).orElse(null);

        if (balance == null) {
            balance = new HrLeaveBalance();
            balance.setEmployee(employee);
            balance.setLeaveType(leaveType);
            balance.setYear(request.getYear());
            balance.setTotalDays(request.getTotalDays());
            balance.setUsedDays(BigDecimal.ZERO);
            balance.setPendingDays(BigDecimal.ZERO);
            balance.setCarriedOverDays(BigDecimal.ZERO);

            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
            balance.setWorkspace(workspace);
        } else {
            balance.setTotalDays(request.getTotalDays());
        }

        return LeaveBalanceResponse.from(leaveBalanceRepository.save(balance));
    }

    @Override
    @Transactional
    public void deductBalance(Long employeeId, Long leaveTypeId, Integer year, BigDecimal days, Long workspaceId) {
        HrLeaveBalance balance = getOrCreateBalance(employeeId, leaveTypeId, year, workspaceId);
        balance.setUsedDays(balance.getUsedDays().add(days));
        leaveBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public void restoreBalance(Long employeeId, Long leaveTypeId, Integer year, BigDecimal days, Long workspaceId) {
        HrLeaveBalance balance = getOrCreateBalance(employeeId, leaveTypeId, year, workspaceId);
        balance.setUsedDays(balance.getUsedDays().subtract(days).max(BigDecimal.ZERO));
        leaveBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public void pendingDeduction(Long employeeId, Long leaveTypeId, Integer year, BigDecimal days, Long workspaceId) {
        HrLeaveBalance balance = getOrCreateBalance(employeeId, leaveTypeId, year, workspaceId);
        balance.setPendingDays(balance.getPendingDays().add(days));
        leaveBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public void pendingRestore(Long employeeId, Long leaveTypeId, Integer year, BigDecimal days, Long workspaceId) {
        HrLeaveBalance balance = getOrCreateBalance(employeeId, leaveTypeId, year, workspaceId);
        balance.setPendingDays(balance.getPendingDays().subtract(days).max(BigDecimal.ZERO));
        leaveBalanceRepository.save(balance);
    }

    private HrLeaveBalance getOrCreateBalance(Long employeeId, Long leaveTypeId, Integer year, Long workspaceId) {
        return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseGet(() -> {
                    Employee employee = employeeRepository.findById(employeeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
                    HrLeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

                    HrLeaveBalance balance = new HrLeaveBalance();
                    balance.setEmployee(employee);
                    balance.setLeaveType(leaveType);
                    balance.setYear(year);
                    balance.setTotalDays(leaveType.getDefaultDays() != null ? BigDecimal.valueOf(leaveType.getDefaultDays()) : BigDecimal.ZERO);
                    balance.setUsedDays(BigDecimal.ZERO);
                    balance.setPendingDays(BigDecimal.ZERO);
                    balance.setCarriedOverDays(BigDecimal.ZERO);
                    Workspace workspace = workspaceRepository.findById(workspaceId)
                            .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
                    balance.setWorkspace(workspace);
                    return leaveBalanceRepository.save(balance);
                });
    }

    private LeaveBalanceResponse createZeroBalance(Long employeeId, Long leaveTypeId, Integer year) {
        HrLeaveType leaveType = leaveTypeRepository.findById(leaveTypeId).orElse(null);
        return LeaveBalanceResponse.builder()
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .leaveTypeName(leaveType != null ? leaveType.getName() : null)
                .year(year)
                .totalDays(BigDecimal.ZERO)
                .usedDays(BigDecimal.ZERO)
                .pendingDays(BigDecimal.ZERO)
                .carriedOverDays(BigDecimal.ZERO)
                .availableDays(BigDecimal.ZERO)
                .build();
    }
}
