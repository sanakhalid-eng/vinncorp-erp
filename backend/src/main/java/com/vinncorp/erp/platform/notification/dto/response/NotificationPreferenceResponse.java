package com.vinncorp.erp.platform.notification.dto.response;
import lombok.Data;
import java.time.LocalDateTime;
@Data

public class NotificationPreferenceResponse {
private Long id;
private boolean taskAssigned;
private boolean taskUnassigned;
private boolean taskStatusChanged;
private boolean taskCreated;
private boolean commentMentioned;
private boolean commentCreated;
private boolean fileUploaded;
private boolean dueDateReminder;
private boolean emailNotifications;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
} 