package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.entity.UserSummary;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.response.NotificationResponse;
import com.vinncorp.erp.modules.projects.entity.Notification;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.enums.NotificationCategory;
import com.vinncorp.erp.modules.projects.enums.NotificationType;
import com.vinncorp.erp.modules.projects.repository.NotificationRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.NotificationService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    public NotificationResponse createNotification(
            Long userId, Long senderId, NotificationType type, String message,
            Long entityId, String entityType, Long projectId, String projectName, String actionUrl
    ) {
        return createNotification(userId, senderId, type, message, entityId, entityType, projectId, projectName, actionUrl, null, null, "MEDIUM");
    }

    @Override
    public NotificationResponse createNotification(
            Long userId, Long senderId, NotificationType type, String message,
            Long entityId, String entityType, Long projectId, String projectName,
            String actionUrl, String eventId, String groupKey, String priority
    ) {
        if (userId == null) return null;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User sender = senderId != null ? userRepository.findById(senderId).orElse(null) : null;

        if (eventId != null && notificationRepository.existsByEventId(eventId)) {
            log.debug("Duplicate event skipped: {}", eventId);
            return null;
        }

        Notification notification = new Notification();
        notification.setEventId(eventId);
        notification.setUser(user);
        notification.setActor(sender);
        notification.setType(type);
        notification.setMessage(message);
        notification.setEntityId(entityId);
        notification.setEntityType(entityType);
        notification.setProjectId(projectId);
        notification.setProjectName(projectName);
        notification.setActionUrl(actionUrl);
        notification.setCategory(mapCategory(type));
        notification.setGroupKey(groupKey);
        notification.setChannel("IN_APP");

        try {
            notification.setPriority(Notification.Priority.valueOf(priority.toUpperCase()));
        } catch (IllegalArgumentException e) {
            notification.setPriority(Notification.Priority.MEDIUM);
        }

        if (type == NotificationType.DUE_SOON || type == NotificationType.DUE_OVERDUE) {
            notification.setExpiresAt(LocalDateTime.now().plusHours(24));
        }

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Override
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        return notificationRepository.markAsRead(notificationId, userId) > 0;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own notifications");
        }

        notificationRepository.delete(notification);
    }

    @Override
    @Scheduled(cron = "0 0 */6 * * ?")
    @Transactional
    public void sendDueDateReminders() {
        log.info("Running due date reminder check...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);
        LocalDateTime yesterday = now.minusHours(24);

        List<Task> dueSoonTasks = taskRepository.findByDueDateBetweenAndAssigneeIsNotNull(yesterday, tomorrow);

        for (Task task : dueSoonTasks) {
            Long assigneeId = task.getAssignee().getId();

            boolean alreadyNotified = notificationRepository.existsByEntityIdAndTypeAndUserIdAndCreatedAtAfter(
                    task.getId(),
                    task.getDueDate().isBefore(now) ? NotificationType.DUE_OVERDUE.name() : NotificationType.DUE_SOON.name(),
                    assigneeId,
                    now.minusHours(24)
            );

            if (alreadyNotified) continue;

            boolean isOverdue = task.getDueDate().isBefore(now);
            NotificationType type = isOverdue ? NotificationType.DUE_OVERDUE : NotificationType.DUE_SOON;
            String message = isOverdue
                    ? "Task \"" + truncate(task.getTitle(), 50) + "\" is overdue"
                    : "Task \"" + truncate(task.getTitle(), 50) + "\" is due soon";

            createNotification(
                    assigneeId, null, type, message,
                    task.getId(), "TASK",
                    task.getProject() != null ? task.getProject().getId() : null,
                    task.getProject() != null ? task.getProject().getName() : null,
                    "/projects/" + task.getProject().getId() + "/board",
                    "due-" + task.getId() + "-" + (isOverdue ? "overdue" : "soon") + "-" + now.toLocalDate(),
                    type.name() + ":" + task.getId(),
                    isOverdue ? "HIGH" : "MEDIUM"
            );

            if (task.getCreatedBy() != null && !task.getCreatedBy().equals(assigneeId)) {
                String creatorMsg = isOverdue
                        ? "Task \"" + truncate(task.getTitle(), 50) + "\" (assigned to " + task.getAssignee().getName() + ") is overdue"
                        : "Task \"" + truncate(task.getTitle(), 50) + "\" (assigned to " + task.getAssignee().getName() + ") is due soon";

                createNotification(
                        task.getCreatedBy(), null, type, creatorMsg,
                        task.getId(), "TASK",
                        task.getProject() != null ? task.getProject().getId() : null,
                        task.getProject() != null ? task.getProject().getName() : null,
                        "/projects/" + task.getProject().getId() + "/board",
                        "due-creator-" + task.getId() + "-" + (isOverdue ? "overdue" : "soon") + "-" + now.toLocalDate(),
                        type.name() + ":" + task.getId(),
                        isOverdue ? "HIGH" : "MEDIUM"
                );
            }
        }

        log.info("Due date reminder check completed. Processed {} tasks.", dueSoonTasks.size());
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setMessage(notification.getMessage());
        response.setEntityId(notification.getEntityId());
        response.setEntityType(notification.getEntityType());
        response.setProjectId(notification.getProjectId());
        response.setProjectName(notification.getProjectName());
        response.setRead(notification.isRead());
        response.setActionUrl(notification.getActionUrl());
        response.setCreatedAt(notification.getCreatedAt());
        response.setCategory(notification.getCategory());

        if (notification.getActor() != null) {
            UserSummary sender = new UserSummary();
            sender.setId(notification.getActor().getId());
            sender.setName(notification.getActor().getName());
            sender.setEmail(notification.getActor().getEmail());
            sender.setAvatarUrl(notification.getActor().getAvatarUrl());
            response.setSender(sender);
        }

        return response;
    }

    @Override
    public Page<NotificationResponse> getFilteredNotifications(Long userId, String category, String type, Pageable pageable) {
        NotificationCategory cat = null;
        if (category != null && !category.isEmpty() && !category.equals("ALL")) {
            try {
                cat = NotificationCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                cat = null;
            }
        }
        String typeFilter = (type != null && !type.isEmpty()) ? type : null;
        return notificationRepository.findByFilters(userId, cat, typeFilter, pageable)
                .map(this::toResponse);
    }

    @Override
    public Map<String, Long> getUnreadCountByCategory(Long userId) {
        List<Object[]> results = notificationRepository.countUnreadByCategory(userId);
        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            NotificationCategory cat = (NotificationCategory) row[0];
            Long count = (Long) row[1];
            counts.put(cat != null ? cat.name() : "UNCATEGORIZED", count);
        }
        return counts;
    }

    private NotificationCategory mapCategory(NotificationType type) {
        if (type == null) return null;
        return switch (type) {
            case TASK_ASSIGNED, TASK_UNASSIGNED, STATUS_CHANGED, DUE_SOON, DUE_OVERDUE, DEADLINE_APPROACHING, FILE_UPLOADED, RECURRING_TASK_GENERATED, RECURRENCE_PAUSED, RECURRENCE_STOPPED -> NotificationCategory.TASK;
            case COMMENT_MENTION, COMMENT_REPLY -> NotificationCategory.COMMENT;
            case PROJECT_INVITE -> NotificationCategory.INVITATION;
            case OWNERSHIP_CHANGED, AUTOMATION_TRIGGERED, AUTOMATION_FAILED, SLA_BREACHED, SLA_WARNING, TASK_ESCALATED, SMART_ASSIGNMENT,
                 EXECUTION_RISK, DELIVERY_DELAY, SPRINT_OVERLOAD, CRITICAL_PATH_ALERT -> NotificationCategory.SYSTEM;
            case LEAVE_REQUESTED, LEAVE_APPROVED, LEAVE_REJECTED, LEAVE_CANCELLED -> NotificationCategory.LEAVE;
        };
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}



