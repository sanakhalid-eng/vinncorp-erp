package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_templates")
public class ProjectTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 50)
    private String icon;

    @Column(name = "has_sprints", nullable = false)
    private boolean hasSprints = false;

    @Column(name = "default_labels", columnDefinition = "TEXT")
    private String defaultLabels;

    @Column(name = "default_columns", columnDefinition = "TEXT")
    private String defaultColumns;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}



