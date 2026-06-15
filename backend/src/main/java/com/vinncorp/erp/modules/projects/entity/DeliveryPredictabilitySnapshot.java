package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "delivery_predictability_snapshots", indexes = {
        @Index(name = "idx_delpred_ws_proj_created", columnList = "workspace_id, project_id, created_at")
})
@Getter
@Setter
public class DeliveryPredictabilitySnapshot extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "predictability_score", nullable = false)
    private double predictabilityScore;

    @Column(name = "on_time_delivery_rate", nullable = false)
    private double onTimeDeliveryRate;

    @Column(name = "avg_delay_days", nullable = false)
    private double avgDelayDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;
}



