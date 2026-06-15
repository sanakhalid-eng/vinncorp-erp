package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_jobs")
@Data
public class ScheduledJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jobName;

    @Column(length = 500)
    private String description;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_duration_ms")
    private Long lastDurationMs;

    @Column(name = "last_status")
    private String lastStatus;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Column(name = "total_runs", nullable = false)
    private long totalRuns = 0;

    @Column(name = "success_runs", nullable = false)
    private long successRuns = 0;

    @Column(name = "failure_runs", nullable = false)
    private long failureRuns = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}



