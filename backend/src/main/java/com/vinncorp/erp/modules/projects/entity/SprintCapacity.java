package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sprint_capacities", indexes = {
        @Index(name = "idx_capacity_sprint", columnList = "sprint_id"),
        @Index(name = "idx_capacity_user", columnList = "user_id"),
        @Index(name = "idx_capacity_workspace", columnList = "workspace_id")
})
@Getter
@Setter
public class SprintCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sprint_id", nullable = false)
    private Long sprintId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "available_hours", nullable = false)
    private double availableHours;

    @Column(name = "allocated_hours", nullable = false)
    private double allocatedHours;

    @Column(name = "utilization_percent", nullable = false)
    private double utilizationPercent;

    @Column(name = "pto_days", nullable = false)
    private int ptoDays;

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



