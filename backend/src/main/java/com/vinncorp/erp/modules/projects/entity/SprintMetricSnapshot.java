package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sprint_metric_snapshots", indexes = {
        @Index(name = "idx_metric_sprint", columnList = "sprint_id"),
        @Index(name = "idx_metric_snapshot_date", columnList = "snapshot_date"),
        @Index(name = "idx_metric_workspace", columnList = "workspace_id")
})
@Getter
@Setter
public class SprintMetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sprint_id", nullable = false)
    private Long sprintId;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "remaining_tasks", nullable = false)
    private int remainingTasks;

    @Column(name = "remaining_points", nullable = false)
    private int remainingPoints;

    @Column(name = "completed_tasks", nullable = false)
    private int completedTasks;

    @Column(name = "completed_points", nullable = false)
    private int completedPoints;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}



