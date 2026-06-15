package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.DependencyType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_dependencies", indexes = {
        @Index(name = "idx_task_id", columnList = "task_id"),
        @Index(name = "idx_depends_on_task_id", columnList = "depends_on_task_id"),
        @Index(name = "idx_dependency_type", columnList = "dependency_type"),
        @Index(name = "idx_task_dep_type", columnList = "task_id, dependency_type"),
        @Index(name = "idx_depends_on_task_dep_type", columnList = "depends_on_task_id, dependency_type")
})
@Data
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOnTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false, length = 20)
    private DependencyType dependencyType = DependencyType.BLOCKED_BY;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false)
    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete(Long byUserId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = byUserId;
    }
}



