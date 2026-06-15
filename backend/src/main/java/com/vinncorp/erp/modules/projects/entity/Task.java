package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.audit.BaseAuditableEntity;

import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_parent_task_id", columnList = "parent_task_id")
})
@Data
public class Task extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id")
    private BoardColumn column;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "workflow_status_id")
    private WorkflowStatus statusEntity;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Column(name = "story_points")
    private Integer storyPoints;

    private LocalDateTime dueDate;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    private List<Task> subtasks = new ArrayList<>();

    @Column(name = "subtask_count", nullable = false)
    private int subtaskCount = 0;

    @Column(name = "completed_subtask_count", nullable = false)
    private int completedSubtaskCount = 0;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskLabel> taskLabels = new ArrayList<>();

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;
}



