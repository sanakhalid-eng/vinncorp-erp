package com.vinncorp.erp.core.workspace.entity;

import com.vinncorp.erp.core.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "workspace_roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class WorkspaceRole extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Workspace workspace;

    @Column(name = "system_managed", nullable = false)
    private boolean systemManaged = false;

    @Column(name = "permissions_json", columnDefinition = "TEXT")
    private String permissionsJson;
}

