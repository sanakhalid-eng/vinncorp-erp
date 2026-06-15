package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.NotificationCategory;
import com.vinncorp.erp.modules.projects.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import com.vinncorp.erp.core.user.entity.UserSummary;

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



