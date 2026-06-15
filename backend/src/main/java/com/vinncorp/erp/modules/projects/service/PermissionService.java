package com.vinncorp.erp.modules.projects.service;

public interface PermissionService {

    boolean hasPermission(Long userId, Long projectId, String permissionName);

    void evictUserProjectPermissions(Long userId, Long projectId);

    void evictProjectPermissionsInWorkspace(Long projectId);

    void evictUserPermissions(Long userId);
}



