package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "capacity_forecast_snapshots", indexes = {
        @Index(name = "idx_capfc_ws_proj_sprint_created",
                columnList = "workspace_id, project_id, sprint_id, created_at")
})
@Getter
@Setter
public class CapacityForecastSnapshot extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(name = "predicted_utilization", nullable = false)
    private double predictedUtilization;

    @Column(name = "predicted_overload_members", nullable = false)
    private int predictedOverloadMembers;

    @Column(name = "recommended_capacity_hours", nullable = false)
    private double recommendedCapacityHours;

    @Column(name = "forecast_horizon_days", nullable = false)
    private int forecastHorizonDays = 14;
}



