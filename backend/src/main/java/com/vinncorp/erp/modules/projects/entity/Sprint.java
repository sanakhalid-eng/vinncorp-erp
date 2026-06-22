package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sprints", indexes = {
        @Index(name = "idx_sprint_project_id", columnList = "project_id"),
        @Index(name = "idx_sprint_status", columnList = "status")
})
@Data
public class Sprint extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String name;

    @Column(length = 2000)
    private String goal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private SprintStatus status;

    @Column(name = "summary_total_tasks")
    private Integer summaryTotalTasks;

    @Column(name = "summary_completed_tasks")
    private Integer summaryCompletedTasks;

    @Column(name = "summary_carried_forward")
    private Integer summaryCarriedForward;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}



