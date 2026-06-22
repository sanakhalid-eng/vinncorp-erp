package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "critical_path_snapshots", indexes = {
        @Index(name = "idx_critpath_workspace", columnList = "workspace_id"),
        @Index(name = "idx_critpath_project", columnList = "project_id"),
        @Index(name = "idx_critpath_task", columnList = "task_id"),
        @Index(name = "idx_critpath_calculated", columnList = "calculated_at")
})
@Getter
@Setter
public class CriticalPathSnapshot extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "dependency_depth", nullable = false)
    private int dependencyDepth;

    @Column(name = "criticality_score", nullable = false)
    private double criticalityScore;

    @Column(name = "is_on_critical_path", nullable = false)
    private boolean isOnCriticalPath;

    @CreationTimestamp
    @Column(name = "calculated_at", nullable = false, updatable = false)
    private LocalDateTime calculatedAt;
}



