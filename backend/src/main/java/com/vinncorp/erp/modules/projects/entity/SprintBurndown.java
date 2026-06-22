package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sprint_burndown", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sprint_burndown_sprint_date", columnNames = {"sprint_id", "date"})
}, indexes = {
        @Index(name = "idx_sprint_burndown_sprint_id", columnList = "sprint_id"),
        @Index(name = "idx_sprint_burndown_date", columnList = "date")
})
@Data
public class SprintBurndown extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_tasks", nullable = false)
    private Integer totalTasks;

    @Column(name = "completed_tasks", nullable = false)
    private Integer completedTasks;

    @Column(name = "remaining_tasks", nullable = false)
    private Integer remainingTasks;

    @Column(name = "blocked_tasks", nullable = false)
    private Integer blockedTasks;
}



