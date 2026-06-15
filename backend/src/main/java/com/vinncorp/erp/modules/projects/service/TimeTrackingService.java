package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.request.TimeLogRequest;
import com.vinncorp.erp.modules.projects.entity.ActiveTimer;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TimeLog;
import com.vinncorp.erp.modules.projects.entity.TimesheetApproval;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.repository.ActiveTimerRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TimeLogRepository;
import com.vinncorp.erp.modules.projects.repository.TimesheetApprovalRepository;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.exception.AppSecurityException;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrackingService {

    private final TimeLogRepository timeLogRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActiveTimerRepository activeTimerRepository;
    private final TimesheetApprovalRepository timesheetApprovalRepository;
    private final ActivityLogService activityLogService;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;

    @Transactional
    public TimeLog logTime(Long taskId, Long userId, TimeLogRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Long workspaceId = resolveWorkspaceId();
        if (task.getProject().getWorkspace() == null || !task.getProject().getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Task does not belong to the current workspace");
        }

        // Check if timesheet is approved for this period
        LocalDate logDate = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();
        LocalDate weekStart = logDate.minusDays(logDate.getDayOfWeek().getValue() - 1);
        Optional<TimesheetApproval> approval = timesheetApprovalRepository.findByUserAndWeekStart(user, weekStart);
        if (approval.isPresent() && approval.get().getStatus() == TimesheetApproval.ApprovalStatus.APPROVED) {
            throw new AppSecurityException("Cannot log time - timesheet is already approved for this week");
        }

        // Calculate hours from time range if provided
        BigDecimal hoursToLog;
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                throw new IllegalArgumentException("End time must be after start time");
            }

            long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
            if (seconds <= 0) {
                throw new IllegalArgumentException("Invalid time range");
            }
            hoursToLog = BigDecimal.valueOf(seconds / 3600.0).setScale(4, RoundingMode.HALF_UP);
            validateHours(hoursToLog);
        } else {
            hoursToLog = request.getHours();
            validateHours(hoursToLog);
        }

        // Validate daily hour limits
        BigDecimal currentDayHours = timeLogRepository.getTotalHoursByUserIdAndDate(userId, logDate);
        if (currentDayHours == null) {
            currentDayHours = BigDecimal.ZERO;
        }

        BigDecimal totalAfterLog = currentDayHours.add(hoursToLog);

        // Hard limit: 24 hours per day
        if (totalAfterLog.compareTo(new BigDecimal("24")) > 0) {
            throw new IllegalArgumentException("Total hours for " + logDate + " would exceed 24 hours limit");
        }

        // Soft limit: 8 hours per day - log warning
        if (totalAfterLog.compareTo(new BigDecimal("8")) > 0) {
            log.warn("User {} has logged more than 8 hours on {}", userId, logDate);
        }

        TimeLog timeLog = new TimeLog();
        timeLog.setTask(task);
        timeLog.setUser(user);
        timeLog.setHours(hoursToLog);
        timeLog.setDescription(request.getDescription());
        timeLog.setLogDate(logDate);
        timeLog.setStartTime(startTime);
        timeLog.setEndTime(endTime);
        timeLog.setWorkspace(task.getProject().getWorkspace());

        TimeLog saved = timeLogRepository.save(timeLog);

        // Log activity
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("hours", hoursToLog.toString());
        activityLogService.logActivity(userId, EntityType.TIME_LOG, saved.getId(),
                ActionType.TIME_LOG_CREATED, null, newValue, "Time logged: " + hoursToLog + " hours", null);

        log.info("Time logged: {} hours for task {} by user {}", hoursToLog, taskId, userId);
        return saved;
    }

    public List<TimeLog> getTaskTimeLogs(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        Long workspaceId = resolveWorkspaceId();
        if (task.getProject().getWorkspace() == null || !task.getProject().getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Task does not belong to the current workspace");
        }
        return timeLogRepository.findByTaskIdOrderByLogDateDescCreatedAtDesc(taskId);
    }

    public List<TimeLog> getUserTimeLogs(Long userId) {
        Long workspaceId = resolveWorkspaceId();
        return timeLogRepository.findByUserIdAndWorkspaceIdOrderByLogDateDescCreatedAtDesc(userId, workspaceId);
    }

    @Transactional
    public TimeLog updateTimeLog(Long logId, Long userId, TimeLogRequest request) {
        TimeLog timeLog = timeLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Time log not found: " + logId));

        Long workspaceId = resolveWorkspaceId();
        if (timeLog.getWorkspace() == null || !timeLog.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Time log does not belong to the current workspace");
        }

        // Check permissions: user can edit own logs, or admin can edit any
        if (!timeLog.getUser().getId().equals(userId)) {
            throw new AppSecurityException("You can only edit your own time logs");
        }

        // Check if timesheet is approved for this period
        LocalDate weekStart = timeLog.getLogDate().minusDays(timeLog.getLogDate().getDayOfWeek().getValue() - 1);
        User user = timeLog.getUser();
        Optional<TimesheetApproval> approval = timesheetApprovalRepository.findByUserAndWeekStart(user, weekStart);
        if (approval.isPresent() && approval.get().getStatus() == TimesheetApproval.ApprovalStatus.APPROVED) {
            throw new AppSecurityException("Cannot edit time log - timesheet is already approved for this week");
        }

        // Calculate new hours from time range if provided
        BigDecimal hoursToLog;
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("hours", timeLog.getHours().toString());

        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                throw new IllegalArgumentException("End time must be after start time");
            }

            long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
            if (seconds <= 0) {
                throw new IllegalArgumentException("Invalid time range");
            }
            hoursToLog = BigDecimal.valueOf(seconds / 3600.0).setScale(4, RoundingMode.HALF_UP);
            validateHours(hoursToLog);
        } else if (request.getHours() != null) {
            hoursToLog = request.getHours();
            validateHours(hoursToLog);
        } else {
            hoursToLog = timeLog.getHours();
        }

        timeLog.setHours(hoursToLog);
        timeLog.setDescription(request.getDescription());
        if (request.getLogDate() != null) {
            timeLog.setLogDate(request.getLogDate());
        }
        timeLog.setStartTime(startTime);
        timeLog.setEndTime(endTime);

        TimeLog saved = timeLogRepository.save(timeLog);

        // Log activity
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("hours", hoursToLog.toString());
        activityLogService.logActivity(userId, EntityType.TIME_LOG, saved.getId(),
                ActionType.TIME_LOG_UPDATED, oldValue, newValue, "Time log updated", null);

        log.info("Time log updated: {} for task {}", logId, timeLog.getTask().getId());
        return saved;
    }

    @Transactional
    public void deleteTimeLog(Long logId, Long userId, boolean isAdmin) {
        TimeLog timeLog = timeLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Time log not found: " + logId));

        Long workspaceId = resolveWorkspaceId();
        if (timeLog.getWorkspace() == null || !timeLog.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Time log does not belong to the current workspace");
        }

        // Check permissions: user can delete own logs, or admin can delete any
        if (!isAdmin && !timeLog.getUser().getId().equals(userId)) {
            throw new AppSecurityException("You can only delete your own time logs");
        }

        // Check if timesheet is approved for this period
        LocalDate weekStart = timeLog.getLogDate().minusDays(timeLog.getLogDate().getDayOfWeek().getValue() - 1);
        User user = timeLog.getUser();
        Optional<TimesheetApproval> approval = timesheetApprovalRepository.findByUserAndWeekStart(user, weekStart);
        if (approval.isPresent() && approval.get().getStatus() == TimesheetApproval.ApprovalStatus.APPROVED) {
            throw new AppSecurityException("Cannot delete time log - timesheet is already approved for this week");
        }

        // Log activity before deleting
        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("hours", timeLog.getHours().toString());
        activityLogService.logActivity(userId, EntityType.TIME_LOG, logId,
                ActionType.TIME_LOG_DELETED, oldValue, null, "Time log deleted", null);

        timeLogRepository.delete(timeLog);
        log.info("Time log deleted: {} by user {}", logId, userId);
    }

    public BigDecimal getTaskTotalHours(Long taskId) {
        BigDecimal total = timeLogRepository.getTotalHoursByTaskId(taskId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<Object[]> getUserTimesheet(Long userId, LocalDate startDate, LocalDate endDate) {
        Long workspaceId = resolveWorkspaceId();
        return timeLogRepository.getDailyHoursByUserAndDateRangeAndWorkspace(userId, workspaceId, startDate, endDate);
    }

    public List<Object[]> getTaskHoursForTimesheet(Long userId, LocalDate startDate, LocalDate endDate) {
        Long workspaceId = resolveWorkspaceId();
        return timeLogRepository.getHoursByTaskForUserAndDateRangeAndWorkspace(userId, workspaceId, startDate, endDate);
    }

    private void validateHours(BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hours must be greater than 0");
        }
        if (hours.compareTo(new BigDecimal("24")) > 0) {
            throw new IllegalArgumentException("Hours cannot exceed 24 per day");
        }
    }

    private Long resolveWorkspaceId() {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (workspaceId == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                workspaceId = currentWorkspaceResolver.resolveDefaultWorkspace(userDetails.getUserId())
                        .map(Workspace::getId)
                        .orElseThrow(() -> new BadRequestException("No workspace context available"));
            } else {
                throw new BadRequestException("No workspace context available");
            }
        }
        return workspaceId;
    }

    @Transactional
    public ActiveTimer startTimer(Long taskId, Long userId, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Check if user already has an active timer
        Optional<ActiveTimer> existingTimer = activeTimerRepository.findByUser(user);
        if (existingTimer.isPresent()) {
            throw new IllegalStateException("User already has an active timer running");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        Long workspaceId = resolveWorkspaceId();
        if (task.getProject().getWorkspace() == null || !task.getProject().getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Task does not belong to the current workspace");
        }

        ActiveTimer timer = new ActiveTimer();
        timer.setUser(user);
        timer.setTask(task);
        timer.setStartedAt(LocalDateTime.now());
        timer.setDescription(description);

        ActiveTimer saved = activeTimerRepository.save(timer);

        // Log activity
        activityLogService.logActivity(userId, EntityType.TIME_LOG, saved.getId(),
                ActionType.TIMER_STARTED, null, null, "Timer started", null);

        log.info("Timer started for task {} by user {}", taskId, userId);
        return saved;
    }

    @Transactional
    public TimeLog stopTimer(Long userId, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ActiveTimer timer = activeTimerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("No active timer found"));

        LocalDateTime now = LocalDateTime.now();
        BigDecimal hoursLogged = BigDecimal.valueOf(
                java.time.Duration.between(timer.getStartedAt(), now).toSeconds() / 3600.0
        ).setScale(4, RoundingMode.HALF_UP);

        if (hoursLogged.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Timer duration is too short");
        }

        // Create time log
        TimeLog timeLog = new TimeLog();
        timeLog.setTask(timer.getTask());
        timeLog.setUser(user);
        timeLog.setHours(hoursLogged);
        timeLog.setDescription(description != null ? description : timer.getDescription());
        timeLog.setLogDate(LocalDate.now());
        timeLog.setWorkspace(timer.getTask().getProject().getWorkspace());

        TimeLog savedLog = timeLogRepository.save(timeLog);

        // Delete active timer
        activeTimerRepository.delete(timer);

        // Log activity
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("hours", hoursLogged.toString());
        activityLogService.logActivity(userId, EntityType.TIME_LOG, savedLog.getId(),
                ActionType.TIMER_STOPPED, null, newValue, "Timer stopped, logged " + hoursLogged + " hours", null);

        log.info("Timer stopped: {} hours logged for task {} by user {}", hoursLogged, timer.getTask().getId(), userId);
        return savedLog;
    }

    public Optional<ActiveTimer> getActiveTimer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return activeTimerRepository.findByUser(user);
    }

    public boolean hasActiveTimer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return activeTimerRepository.existsByUser(user);
    }

    // Timesheet Approval Workflow
    @Transactional
    public TimesheetApproval submitTimesheet(Long userId, LocalDate weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDate weekEnd = weekStart.plusDays(6);

        // Check if already submitted
        Optional<TimesheetApproval> existing = timesheetApprovalRepository.findByUserAndWeekStart(user, weekStart);
        if (existing.isPresent()) {
            throw new IllegalStateException("Timesheet already submitted for this week");
        }

        TimesheetApproval approval = new TimesheetApproval();
        approval.setUser(user);
        approval.setWeekStart(weekStart);
        approval.setWeekEnd(weekEnd);
        approval.setStatus(TimesheetApproval.ApprovalStatus.PENDING);

        TimesheetApproval saved = timesheetApprovalRepository.save(approval);

        // Log activity
        activityLogService.logActivity(userId, EntityType.TIMESHEET_APPROVAL, saved.getId(),
                ActionType.CREATED, null, null, "Timesheet submitted for approval", null);

        log.info("Timesheet submitted for user {} week starting {}", userId, weekStart);
        return saved;
    }

    @Transactional
    public TimesheetApproval approveTimesheet(Long approvalId, Long approverId) {
        TimesheetApproval approval = timesheetApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet approval not found: " + approvalId));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found: " + approverId));

        String oldStatus = approval.getStatus().toString();

        approval.setStatus(TimesheetApproval.ApprovalStatus.APPROVED);
        approval.setApprovedBy(approver);
        approval.setApprovedAt(LocalDateTime.now());

        TimesheetApproval saved = timesheetApprovalRepository.save(approval);

        // Log activity
        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("status", oldStatus);
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("status", "APPROVED");
        activityLogService.logActivity(approverId, EntityType.TIMESHEET_APPROVAL, approvalId,
                ActionType.UPDATED, oldValue, newValue, "Timesheet approved", null);

        log.info("Timesheet approved: {} by user {}", approvalId, approverId);
        return saved;
    }

    @Transactional
    public TimesheetApproval rejectTimesheet(Long approvalId, Long approverId, String reason) {
        TimesheetApproval approval = timesheetApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet approval not found: " + approvalId));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found: " + approverId));

        String oldStatus = approval.getStatus().toString();

        approval.setStatus(TimesheetApproval.ApprovalStatus.REJECTED);
        approval.setApprovedBy(approver);
        approval.setApprovedAt(LocalDateTime.now());
        approval.setRejectionReason(reason);

        TimesheetApproval saved = timesheetApprovalRepository.save(approval);

        // Log activity
        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("status", oldStatus);
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("status", "REJECTED");
        activityLogService.logActivity(approverId, EntityType.TIMESHEET_APPROVAL, approvalId,
                ActionType.UPDATED, oldValue, newValue, "Timesheet rejected: " + reason, null);

        log.info("Timesheet rejected: {} by user {}", approvalId, approverId);
        return saved;
    }

    public List<TimesheetApproval> getPendingApprovals() {
        return timesheetApprovalRepository.findByStatus(TimesheetApproval.ApprovalStatus.PENDING);
    }

    public List<TimesheetApproval> getUserTimesheets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return timesheetApprovalRepository.findByUserAndStatus(user, TimesheetApproval.ApprovalStatus.PENDING);
    }
}



