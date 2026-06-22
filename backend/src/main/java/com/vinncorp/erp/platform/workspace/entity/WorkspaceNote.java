package com.vinncorp.erp.platform.workspace.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workspace_notes", indexes = {
        @Index(name = "idx_notes_ws_proj_created", columnList = "workspace_id, project_id, created_at")
})
@Getter
@Setter
public class WorkspaceNote extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean pinned;
}

