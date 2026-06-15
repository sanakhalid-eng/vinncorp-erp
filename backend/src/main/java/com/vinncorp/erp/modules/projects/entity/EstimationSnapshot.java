package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estimation_snapshots", indexes = {
        @Index(name = "idx_est_workspace", columnList = "workspace_id"),
        @Index(name = "idx_est_project", columnList = "project_id"),
        @Index(name = "idx_est_task", columnList = "task_id"),
        @Index(name = "idx_est_created", columnList = "created_at")
})
@Getter
@Setter
public class EstimationSnapshot extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "estimated_points")
    private Integer estimatedPoints;

    @Column(name = "actual_points")
    private Integer actualPoints;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "estimation_drift")
    private Double estimationDrift;
}



