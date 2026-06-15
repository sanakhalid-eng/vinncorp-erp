package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "monte_carlo_forecasts", indexes = {
        @Index(name = "idx_mc_ws_sprint_created", columnList = "workspace_id, sprint_id, created_at")
})
@Getter
@Setter
public class MonteCarloForecast extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "sprint_id", nullable = false)
    private Long sprintId;

    @Column(nullable = false)
    private int iterations = 1000;

    @Column(name = "p50_completion_date")
    private LocalDate p50CompletionDate;

    @Column(name = "p85_completion_date")
    private LocalDate p85CompletionDate;

    @Column(name = "p95_completion_date")
    private LocalDate p95CompletionDate;

    @Column(name = "mean_remaining_points", nullable = false)
    private double meanRemainingPoints;

    @Column(name = "confidence_score", nullable = false)
    private double confidenceScore;
}



