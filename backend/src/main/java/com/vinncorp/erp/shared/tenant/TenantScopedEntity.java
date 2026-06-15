package com.vinncorp.erp.shared.tenant;

import com.vinncorp.erp.core.workspace.entity.Workspace;

public interface TenantScopedEntity {
    Workspace getWorkspace();
    Long getWorkspaceId();
}

