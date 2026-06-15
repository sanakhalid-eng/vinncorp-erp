package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "portfolio_roadmap_items", indexes = {
        @Index(name = "idx_roadmap_ws_proj_created", columnList = "workspace_id, project_id, created_at")
})
@Getter
@Setter
public class PortfolioRoadmapItem extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "milestone_date")
    private LocalDate milestoneDate;

    @Column(nullable = false, length = 50)
    private String status = "PLANNED";

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}



