package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.SLAStatus;
import com.vinncorp.erp.modules.projects.enums.SLAType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_slas", indexes = {
        @Index(name = "idx_sla_workspace", columnList = "workspace_id"),
        @Index(name = "idx_sla_project", columnList = "project_id"),
        @Index(name = "idx_sla_task", columnList = "task_id"),
        @Index(name = "idx_sla_status", columnList = "status"),
        @Index(name = "idx_sla_breached", columnList = "breached_at")
})
@Getter
@Setter
public class TaskSLA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sla_type", nullable = false, length = 20)
    private SLAType slaType;

    @Column(name = "response_minutes")
    private Integer responseMinutes;

    @Column(name = "completion_minutes")
    private Integer completionMinutes;

    @Column(name = "warning_threshold_pct", nullable = false)
    private double warningThresholdPct = 80.0;

    @Column(name = "breached_at")
    private LocalDateTime breachedAt;

    @Column(name = "warned_at")
    private LocalDateTime warnedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SLAStatus status = SLAStatus.ACTIVE;

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



