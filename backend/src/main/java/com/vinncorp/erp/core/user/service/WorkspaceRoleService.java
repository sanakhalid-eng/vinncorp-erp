package com.vinncorp.erp.core.user.service;

import com.vinncorp.erp.core.user.entity.WorkspaceUserRole;
import com.vinncorp.erp.modules.projects.entity.Role;

import java.util.List;

public interface WorkspaceRoleService {

    /**
     * Get all roles for a user in a specific workspace.
     */
    List<Role> getUserRolesInWorkspace(Long workspaceId, Long userId);

    /**
     * Get all role names for a user in a specific workspace.
     */
    List<String> getUserRoleNamesInWorkspace(Long workspaceId, Long userId);

    /**
     * Get all permission names for a user in a specific workspace (union of all role permissions).
     */
    List<String> getUserPermissionNamesInWorkspace(Long workspaceId, Long userId);

    /**
     * Assign a role to a user in a workspace. Supports multiple roles per user.
     */
    WorkspaceUserRole assignRole(Long workspaceId, Long userId, Long roleId, Long assignedByUserId);

    /**
     * Remove a specific role from a user in a workspace.
     */
    void removeRole(Long workspaceId, Long userId, Long roleId);

    /**
     * Check if a user has a specific role in a workspace.
     */
    boolean hasRoleInWorkspace(Long workspaceId, Long userId, String roleName);

    /**
     * Check if a user has a specific permission in a workspace.
     */
    boolean hasPermissionInWorkspace(Long workspaceId, Long userId, String permissionName);

    /**
     * Get all workspace memberships with roles for a user.
     */
    List<WorkspaceUserRole> getAllByUserId(Long userId);
}
