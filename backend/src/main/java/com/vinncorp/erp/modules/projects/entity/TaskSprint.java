package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_sprints", indexes = {
        @Index(name = "idx_task_sprint_sprint_id", columnList = "sprint_id"),
        @Index(name = "idx_task_sprint_task_id", columnList = "task_id")
})
@Data
public class TaskSprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}



