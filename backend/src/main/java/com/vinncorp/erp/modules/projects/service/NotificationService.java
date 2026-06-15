package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.NotificationResponse;
import com.vinncorp.erp.modules.projects.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NotificationService {

    NotificationResponse createNotification(
            Long userId,
            Long senderId,
            NotificationType type,
            String message,
            Long entityId,
            String entityType,
            Long projectId,
            String projectName,
            String actionUrl
    );

    NotificationResponse createNotification(
            Long userId,
            Long senderId,
            NotificationType type,
            String message,
            Long entityId,
            String entityType,
            Long projectId,
            String projectName,
            String actionUrl,
            String eventId,
            String groupKey,
            String priority
    );

    Page<NotificationResponse> getNotifications(Long userId, Pageable pageable);

    Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    boolean markAsRead(Long notificationId, Long userId);

    int markAllAsRead(Long userId);

    void deleteNotification(Long notificationId, Long userId);

    void sendDueDateReminders();

    Page<NotificationResponse> getFilteredNotifications(Long userId, String category, String type, Pageable pageable);

    Map<String, Long> getUnreadCountByCategory(Long userId);
}



