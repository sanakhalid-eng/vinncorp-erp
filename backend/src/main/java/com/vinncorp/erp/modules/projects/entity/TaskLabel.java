package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_labels",
        uniqueConstraints = @UniqueConstraint(name = "uk_task_label", columnNames = {"task_id", "label_id"}),
        indexes = {
        @Index(name = "idx_task_label_task", columnList = "task_id"),
        @Index(name = "idx_task_label_label", columnList = "label_id")
})
@Data
public class TaskLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}



