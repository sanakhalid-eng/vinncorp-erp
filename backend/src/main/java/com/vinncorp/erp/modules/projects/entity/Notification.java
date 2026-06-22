package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.NotificationCategory;
import com.vinncorp.erp.modules.projects.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_is_read", columnList = "is_read"),
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
        @Index(name = "idx_notification_dedup", columnList = "user_id, type, entity_id"),
        @Index(name = "idx_notification_expires", columnList = "expires_at")
})
@Data
public class Notification extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, length = 100)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private NotificationCategory category;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name", length = 200)
    private String projectName;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "group_key", length = 200)
    private String groupKey;

    @Column(name = "channel")
    private String channel = "IN_APP";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Workspace workspace;

    @Column(name = "workspace_id", insertable = false, updatable = false)
    private Long workspaceId;

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}



