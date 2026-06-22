package com.vinncorp.erp.shared.tenant;

import com.vinncorp.erp.platform.workspace.entity.Workspace;

public interface TenantScopedEntity {
    Workspace getWorkspace();
    Long getWorkspaceId();
}

