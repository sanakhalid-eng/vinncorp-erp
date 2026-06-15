package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "productivity_snapshots", indexes = {
        @Index(name = "idx_prod_workspace", columnList = "workspace_id"),
        @Index(name = "idx_prod_project", columnList = "project_id"),
        @Index(name = "idx_prod_sprint", columnList = "sprint_id"),
        @Index(name = "idx_prod_created", columnList = "created_at")
})
@Getter
@Setter
public class ProductivitySnapshot extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(name = "throughput", nullable = false)
    private int throughput;

    @Column(name = "average_cycle_time", nullable = false)
    private double averageCycleTime;

    @Column(name = "average_lead_time", nullable = false)
    private double averageLeadTime;

    @Column(name = "blocked_time_hours", nullable = false)
    private double blockedTimeHours;

    @Column(name = "predictability_score", nullable = false)
    private double predictabilityScore;
}



