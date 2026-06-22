package com.vinncorp.erp.platform.workspace.service;

import com.vinncorp.erp.platform.workspace.entity.Workspace;

import java.util.Optional;

public interface CurrentWorkspaceResolver {
    Optional<Workspace> resolveCurrentWorkspace();
    Optional<Workspace> resolveDefaultWorkspace(Long userId);
    void setCurrentWorkspace(Long workspaceId);
    Long getCurrentWorkspaceId();
}

