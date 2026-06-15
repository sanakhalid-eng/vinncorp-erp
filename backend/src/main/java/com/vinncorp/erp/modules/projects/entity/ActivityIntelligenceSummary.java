package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import com.vinncorp.erp.core.audit.BaseTenantEntity;

@Entity
@Table(name = "activity_intelligence_summaries", indexes = {
        @Index(name = "idx_actintel_ws_proj_created", columnList = "workspace_id, project_id, created_at")
})
@Getter
@Setter
public class ActivityIntelligenceSummary extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "summary_type", nullable = false, length = 50)
    private String summaryType;

    @Column(name = "highlights_json", columnDefinition = "TEXT")
    private String highlightsJson;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;
}



