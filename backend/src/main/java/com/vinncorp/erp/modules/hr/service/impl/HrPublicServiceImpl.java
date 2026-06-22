package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.dto.response.EmployeeResponse;
import com.vinncorp.erp.modules.hr.service.HrPublicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrPublicServiceImpl implements HrPublicService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> getEmployeeByUserId(Long userId, Long workspaceId) {
        return employeeRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .map(EmployeeResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> getEmployeeById(Long employeeId, Long workspaceId) {
        return employeeRepository.findByIdAndWorkspaceId(employeeId, workspaceId)
                .map(EmployeeResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getActiveEmployees(Long workspaceId) {
        return employeeRepository.findAllByWorkspaceIdAndStatus(workspaceId, EmployeeStatus.ACTIVE)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByStatus(Long workspaceId, EmployeeStatus status) {
        return employeeRepository.findAllByWorkspaceIdAndStatus(workspaceId, status)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployeeActive(Long employeeId, Long workspaceId) {
        return employeeRepository.findByIdAndWorkspaceId(employeeId, workspaceId)
                .map(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional
    public void handleEmployeeStatusChange(Long employeeId, EmployeeStatus oldStatus, EmployeeStatus newStatus, Long workspaceId) {
        log.info("Employee {} status changed from {} to {} in workspace {}", employeeId, oldStatus, newStatus, workspaceId);
    }

    @EventListener
    @Transactional
    public void onLeaveApproved(com.vinncorp.erp.modules.hr.event.LeaveApprovedEvent event) {
        log.info("Leave approved for employee {} in workspace {}", event.getEmployeeId(), event.getWorkspaceId());
    }

    @EventListener
    @Transactional
    public void onLeaveCancelled(com.vinncorp.erp.modules.hr.event.LeaveCancelledEvent event) {
        log.info("Leave cancelled for employee {} in workspace {}", event.getEmployeeId(), event.getWorkspaceId());
    }
}
