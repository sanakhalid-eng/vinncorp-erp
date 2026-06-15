package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "workflow_status")
public class WorkflowStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO, IN_PROGRESS, REVIEW etc
    @Column(nullable = false)
    private String name;

    // UI color
    private String color;

    // ordering in kanban board
    private Integer orderIndex;

    private boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private Integer position;

    private String entityType;
}


