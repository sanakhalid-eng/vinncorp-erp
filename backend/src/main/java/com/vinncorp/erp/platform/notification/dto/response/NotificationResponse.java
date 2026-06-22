package com.vinncorp.erp.platform.notification.dto.response;
import com.vinncorp.erp.platform.notification.enums.NotificationCategory;
import com.vinncorp.erp.platform.notification.enums.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;
import com.vinncorp.erp.platform.user.entity.UserSummary;
@Data

public class NotificationResponse {
private Long id;
private UserSummary sender;
private NotificationType type;
private String message;
private Long entityId;
private String entityType;
private Long projectId;
private String projectName;
private boolean isRead;
private String actionUrl;
private LocalDateTime createdAt;
private NotificationCategory category;
} 