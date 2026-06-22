package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;

import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "execution_risk_snapshots", indexes = {
        @Index(name = "idx_risk_workspace", columnList = "workspace_id"),
        @Index(name = "idx_risk_project", columnList = "project_id"),
        @Index(name = "idx_risk_sprint", columnList = "sprint_id"),
        @Index(name = "idx_risk_created", columnList = "created_at")
})
@Getter
@Setter
public class ExecutionRiskSnapshot extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(name = "risk_score", nullable = false)
    private double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "delayed_task_count", nullable = false)
    private int delayedTaskCount;

    @Column(name = "blocked_task_count", nullable = false)
    private int blockedTaskCount;

    @Column(name = "overloaded_member_count", nullable = false)
    private int overloadedMemberCount;

    @Column(name = "velocity_decline_percent", nullable = false)
    private double velocityDeclinePercent;
}



