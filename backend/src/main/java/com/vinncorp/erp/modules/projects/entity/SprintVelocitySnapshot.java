package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sprint_velocity_snapshots", indexes = {
        @Index(name = "idx_velocity_sprint", columnList = "sprint_id"),
        @Index(name = "idx_velocity_workspace", columnList = "workspace_id"),
        @Index(name = "idx_velocity_project", columnList = "project_id")
})
@Getter
@Setter
public class SprintVelocitySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sprint_id", nullable = false)
    private Long sprintId;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "committed_points", nullable = false)
    private int committedPoints;

    @Column(name = "completed_points", nullable = false)
    private int completedPoints;

    @Column(name = "spillover_points", nullable = false)
    private int spilloverPoints;

    @Column(name = "completion_rate", nullable = false)
    private double completionRate;

    @Column(name = "velocity_score", nullable = false)
    private double velocityScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}



