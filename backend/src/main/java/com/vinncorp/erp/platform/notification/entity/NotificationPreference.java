package com.vinncorp.erp.platform.notification.entity;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
@EqualsAndHashCode(callSuper = true) @Entity
@Table(name = "notification_preferences") @Data

public class NotificationPreference extends BaseAuditableEntity {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
@OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
@Column(name = "task_assigned", nullable = false) private boolean taskAssigned = true;
@Column(name = "task_unassigned", nullable = false) private boolean taskUnassigned = true;
@Column(name = "task_status_changed", nullable = false) private boolean taskStatusChanged = true;
@Column(name = "task_created", nullable = false) private boolean taskCreated = true;
@Column(name = "comment_mentioned", nullable = false) private boolean commentMentioned = true;
@Column(name = "comment_created", nullable = false) private boolean commentCreated = true;
@Column(name = "file_uploaded", nullable = false) private boolean fileUploaded = true;
@Column(name = "due_date_reminder", nullable = false) private boolean dueDateReminder = true;
@Column(name = "email_notifications", nullable = false) private boolean emailNotifications = false;
} 