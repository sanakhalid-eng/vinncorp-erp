package com.vinncorp.erp.core.audit;

import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.shared.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@MappedSuperclass
public abstract class BaseTenantEntity extends BaseAuditableEntity implements TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Workspace workspace;

    @Column(name = "workspace_id", insertable = false, updatable = false)
    private Long workspaceId;

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Long getWorkspaceId() {
        return workspaceId;
    }
}

