package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.RecurrenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_task_templates", indexes = {
        @Index(name = "idx_recurring_template_workspace", columnList = "workspace_id"),
        @Index(name = "idx_recurring_template_next_run", columnList = "next_run_at"),
        @Index(name = "idx_recurring_template_active", columnList = "active"),
        @Index(name = "idx_recurring_template_project", columnList = "project_id")
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class RecurringTaskTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "template_task_id", nullable = false)
    private Long templateTaskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 20)
    private RecurrenceType recurrenceType;

    @Column(name = "interval_value", nullable = false)
    private int intervalValue = 1;

    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "next_run_at", nullable = false)
    private LocalDateTime nextRunAt;

    @Column(name = "last_generated_at")
    private LocalDateTime lastGeneratedAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean paused = false;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "max_occurrences")
    private Integer maxOccurrences;

    @Column(name = "generated_count", nullable = false)
    private int generatedCount = 0;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete(Long byUserId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = byUserId;
    }
}



