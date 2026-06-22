package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "labels", indexes = {
        @Index(name = "idx_label_project", columnList = "project_id"),
        @Index(name = "idx_label_name_project", columnList = "name, project_id", unique = true)
})
@Data
public class Label extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    public void softDelete() {
        setDeletedAt(LocalDateTime.now());
    }
}



