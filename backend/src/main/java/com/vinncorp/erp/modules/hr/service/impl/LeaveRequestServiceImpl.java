package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.entity.HrLeaveRequest;
import com.vinncorp.erp.modules.hr.entity.HrLeaveType;
import com.vinncorp.erp.modules.hr.enums.LeaveRequestStatus;
import com.vinncorp.erp.modules.hr.event.LeaveApprovedEvent;
import com.vinncorp.erp.modules.hr.event.LeaveCancelledEvent;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.repository.HrLeaveRequestRepository;
import com.vinncorp.erp.modules.hr.repository.HrLeaveTypeRepository;
import com.vinncorp.erp.modules.hr.dto.request.LeaveRequestActionRequest;
import com.vinncorp.erp.modules.hr.dto.request.LeaveRequestCreateRequest;
import com.vinncorp.erp.modules.hr.dto.response.LeaveRequestResponse;
import com.vinncorp.erp.modules.hr.service.LeaveBalanceService;
import com.vinncorp.erp.modules.hr.service.LeaveRequestService;
import com.vinncorp.erp.modules.projects.enums.NotificationType;
import com.vinncorp.erp.modules.projects.service.NotificationService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final HrLeaveRequestRepository leaveRequestRepository;
    private final HrLeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final NotificationService notificationService;
    private final ApplicationContext applicationContext;

    @Override
    @Transactional
    public LeaveRequestResponse apply(LeaveRequestCreateRequest request, Long workspaceId, Long currentUserId) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        HrLeaveType leaveType = leaveTypeRepository.findByIdAndWorkspaceId(request.getLeaveTypeId(), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after or equal to start date");
        }

        BigDecimal totalDays = calculateWorkingDays(request.getStartDate(), request.getEndDate());
        if (totalDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Leave must include at least one working day");
        }

        List<LeaveRequestStatus> activeStatuses = List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED);
        List<HrLeaveRequest> overlapping = leaveRequestRepository.findOverlappingRequests(
                request.getEmployeeId(), request.getStartDate(), request.getEndDate(), activeStatuses);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Employee already has a leave request covering these dates");
        }

        HrLeaveRequest leaveRequest = new HrLeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setTotalDays(totalDays);
        leaveRequest.setReason(request.getReason());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest.setYear(request.getStartDate().getYear());

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        leaveRequest.setWorkspace(workspace);

        HrLeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        leaveBalanceService.pendingDeduction(
                employee.getId(), leaveType.getId(), request.getStartDate().getYear(), totalDays, workspaceId);

        notifyManager(employee, workspaceId, currentUserId, saved);

        log.info("Leave request {} created for employee {} by user {}", saved.getId(), employee.getId(), currentUserId);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse approve(Long id, Long workspaceId, Long approvedByUserId) {
        HrLeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be approved");
        }

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprovedBy(approvedByUserId);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        HrLeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        leaveBalanceService.pendingRestore(
                leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId(),
                leaveRequest.getStartDate().getYear(), leaveRequest.getTotalDays(), workspaceId);
        leaveBalanceService.deductBalance(
                leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId(),
                leaveRequest.getStartDate().getYear(), leaveRequest.getTotalDays(), workspaceId);

        sendNotificationToEmployee(leaveRequest, approvedByUserId, workspaceId,
                NotificationType.LEAVE_APPROVED, "Your leave request has been approved");

        applicationContext.publishEvent(new LeaveApprovedEvent(this,
                leaveRequest.getEmployee().getId(), saved.getId(), workspaceId));

        log.info("Leave request {} approved by user {}", id, approvedByUserId);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse reject(Long id, LeaveRequestActionRequest request, Long workspaceId, Long rejectedByUserId) {
        HrLeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be rejected");
        }

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setRejectionReason(request != null ? request.getRejectionReason() : null);
        leaveRequest.setApprovedBy(rejectedByUserId);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        HrLeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        leaveBalanceService.pendingRestore(
                leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId(),
                leaveRequest.getStartDate().getYear(), leaveRequest.getTotalDays(), workspaceId);

        sendNotificationToEmployee(leaveRequest, rejectedByUserId, workspaceId,
                NotificationType.LEAVE_REJECTED, "Your leave request has been rejected");

        log.info("Leave request {} rejected by user {}", id, rejectedByUserId);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse cancel(Long id, Long workspaceId, Long cancelledByUserId) {
        HrLeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leaveRequest.getStatus() == LeaveRequestStatus.CANCELLED) {
            throw new BadRequestException("Leave request is already cancelled");
        }
        if (leaveRequest.getStatus() == LeaveRequestStatus.REJECTED) {
            throw new BadRequestException("Cannot cancel a rejected leave request");
        }

        LeaveRequestStatus previousStatus = leaveRequest.getStatus();
        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        leaveRequest.setCancelledAt(LocalDateTime.now());
        leaveRequest.setCancelledBy(cancelledByUserId);

        HrLeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        if (previousStatus == LeaveRequestStatus.PENDING) {
            leaveBalanceService.pendingRestore(
                    leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId(),
                    leaveRequest.getStartDate().getYear(), leaveRequest.getTotalDays(), workspaceId);
        } else if (previousStatus == LeaveRequestStatus.APPROVED) {
            leaveBalanceService.restoreBalance(
                    leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId(),
                    leaveRequest.getStartDate().getYear(), leaveRequest.getTotalDays(), workspaceId);
        }

        applicationContext.publishEvent(new LeaveCancelledEvent(this,
                leaveRequest.getEmployee().getId(), saved.getId(), workspaceId));

        log.info("Leave request {} cancelled by user {}", id, cancelledByUserId);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    public LeaveRequestResponse get(Long id, Long workspaceId) {
        HrLeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        return LeaveRequestResponse.from(leaveRequest);
    }

    @Override
    public List<LeaveRequestResponse> listByEmployee(Long employeeId, Long workspaceId) {
        return leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)
                .stream().map(LeaveRequestResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestResponse> listByStatus(LeaveRequestStatus status, Long workspaceId) {
        return leaveRequestRepository.findByWorkspaceIdAndStatusOrderByStartDateDesc(workspaceId, status)
                .stream().map(LeaveRequestResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestResponse> listAll(Long workspaceId) {
        return leaveRequestRepository.findByWorkspaceIdOrderByStartDateDesc(workspaceId)
                .stream().map(LeaveRequestResponse::from).collect(Collectors.toList());
    }

    @Override
    public long countPending(Long workspaceId) {
        return leaveRequestRepository.countByWorkspaceIdAndStatus(workspaceId, LeaveRequestStatus.PENDING);
    }

    private BigDecimal calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        BigDecimal days = BigDecimal.ZERO;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                days = days.add(BigDecimal.ONE);
            }
            current = current.plusDays(1);
        }
        return days;
    }

    private void notifyManager(Employee employee, Long workspaceId, Long senderId, HrLeaveRequest leaveRequest) {
        try {
            if (employee.getManagerId() != null) {
                notificationService.createNotification(
                        employee.getManagerId(), senderId,
                        NotificationType.LEAVE_REQUESTED,
                        "New leave request from " + employee.getFullName(),
                        leaveRequest.getId(), "LEAVE_REQUEST",
                        null, null,
                        "/hr/leave-approvals",
                        "leave-" + leaveRequest.getId(),
                        "LEAVE:" + leaveRequest.getId(),
                        "MEDIUM"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send leave request notification: {}", e.getMessage());
        }
    }

    private void sendNotificationToEmployee(HrLeaveRequest leaveRequest, Long senderId, Long workspaceId,
                                            NotificationType type, String message) {
        try {
            if (leaveRequest.getEmployee().getUserId() == null) return;
            User employeeUser = userRepository.findById(leaveRequest.getEmployee().getUserId()).orElse(null);
            if (employeeUser != null) {
                notificationService.createNotification(
                        employeeUser.getId(), senderId,
                        type, message,
                        leaveRequest.getId(), "LEAVE_REQUEST",
                        null, null,
                        "/hr/leave-requests",
                        type.name().toLowerCase() + "-" + leaveRequest.getId(),
                        "LEAVE:" + leaveRequest.getId(),
                        "MEDIUM"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send leave notification: {}", e.getMessage());
        }
    }
}
