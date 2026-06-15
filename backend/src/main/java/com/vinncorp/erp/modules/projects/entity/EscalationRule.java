package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.EscalationTrigger;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "escalation_rules", indexes = {
        @Index(name = "idx_esc_workspace", columnList = "workspace_id"),
        @Index(name = "idx_esc_project", columnList = "project_id"),
        @Index(name = "idx_esc_enabled", columnList = "enabled")
})
@Getter
@Setter
public class EscalationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_condition", nullable = false, length = 50)
    private EscalationTrigger triggerCondition;

    @Column(name = "threshold_minutes", nullable = false)
    private int thresholdMinutes;

    @Column(name = "escalate_to_role", length = 50)
    private String escalateToRole;

    @Column(name = "escalate_to_user_id")
    private Long escalateToUserId;

    @Column(name = "notify_assignee", nullable = false)
    private boolean notifyAssignee = true;

    @Column(name = "notify_project_lead", nullable = false)
    private boolean notifyProjectLead = false;

    @Column(name = "auto_assign", nullable = false)
    private boolean autoAssign = false;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



