package com.vinncorp.erp.core.workspace.service;

import com.vinncorp.erp.core.workspace.entity.Workspace;

import java.util.Optional;

public interface CurrentWorkspaceResolver {
    Optional<Workspace> resolveCurrentWorkspace();
    Optional<Workspace> resolveDefaultWorkspace(Long userId);
    void setCurrentWorkspace(Long workspaceId);
    Long getCurrentWorkspaceId();
}

