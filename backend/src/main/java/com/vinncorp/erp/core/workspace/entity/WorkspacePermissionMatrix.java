package com.vinncorp.erp.core.workspace.entity;

import com.vinncorp.erp.core.user.entity.User;

import com.vinncorp.erp.core.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspace_permission_matrix")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class WorkspacePermissionMatrix extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private WorkspaceRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    @Column(nullable = false)
    private boolean allowed = true;
}

