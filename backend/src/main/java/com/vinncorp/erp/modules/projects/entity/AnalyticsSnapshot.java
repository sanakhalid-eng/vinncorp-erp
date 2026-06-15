package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_snapshots", indexes = {
        @Index(name = "idx_analytics_ws_proj_sprint_created",
                columnList = "workspace_id, project_id, sprint_id, created_at")
})
@Getter
@Setter
public class AnalyticsSnapshot extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(name = "snapshot_type", nullable = false, length = 50)
    private String snapshotType;

    @Column(name = "metrics_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String metricsJson;

    @CreationTimestamp
    @Column(name = "captured_at", nullable = false, updatable = false)
    private LocalDateTime capturedAt;
}



