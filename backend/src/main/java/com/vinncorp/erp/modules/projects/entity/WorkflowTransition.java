package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "workflow_transitions")
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private WorkflowStatus fromStatus;

    @ManyToOne
    private WorkflowStatus toStatus;

    @ManyToOne
    private Project project;

    @Column(columnDefinition = "TEXT")
    private String rule;
}



