package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_task_occurrences", indexes = {
        @Index(name = "idx_recurring_occurrence_template", columnList = "recurring_template_id"),
        @Index(name = "idx_recurring_occurrence_date", columnList = "occurrence_date"),
        @Index(name = "idx_recurring_occurrence_status", columnList = "generation_status")
})
@Getter
@Setter
public class RecurringTaskOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurring_template_id", nullable = false)
    private Long recurringTemplateId;

    @Column(name = "generated_task_id", nullable = false)
    private Long generatedTaskId;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    @Column(name = "generation_status", nullable = false, length = 20)
    private String generationStatus = "GENERATED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}



