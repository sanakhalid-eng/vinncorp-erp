package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.entity.WorkspaceUserRole;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.repository.WorkspaceUserRoleRepository;
import com.vinncorp.erp.platform.user.service.WorkspaceRoleService;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceRoleServiceImpl implements WorkspaceRoleService {

    private final WorkspaceUserRoleRepository workspaceUserRoleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Role> getUserRolesInWorkspace(Long workspaceId, Long userId) {
        return workspaceUserRoleRepository.findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .stream()
                .map(WorkspaceUserRole::getRole)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleNamesInWorkspace(Long workspaceId, Long userId) {
        return workspaceUserRoleRepository.findRoleNamesByWorkspaceAndUser(workspaceId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserPermissionNamesInWorkspace(Long workspaceId, Long userId) {
        return workspaceUserRoleRepository.findPermissionNamesByWorkspaceAndUser(workspaceId, userId);
    }

    @Override
    @Transactional
    public WorkspaceUserRole assignRole(Long workspaceId, Long userId, Long roleId, Long assignedByUserId) {
        if (workspaceUserRoleRepository.existsByWorkspaceIdAndUserIdAndRoleIdAndDeletedAtIsNull(
                workspaceId, userId, roleId)) {
            throw new BadRequestException("User already has this role in the workspace", ErrorCode.CONFLICT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        WorkspaceUserRole wur = new WorkspaceUserRole();
        wur.setWorkspace(new com.vinncorp.erp.platform.workspace.entity.Workspace());
        wur.getWorkspace().setId(workspaceId);
        wur.setUser(user);

        Role role = new Role();
        role.setId(roleId);
        wur.setRole(role);

        if (assignedByUserId != null) {
            User assignedBy = userRepository.findById(assignedByUserId).orElse(null);
            wur.setAssignedBy(assignedBy);
        }

        WorkspaceUserRole saved = workspaceUserRoleRepository.save(wur);
        log.info("Assigned role {} to user {} in workspace {}", roleId, userId, workspaceId);
        return saved;
    }

    @Override
    @Transactional
    public void removeRole(Long workspaceId, Long userId, Long roleId) {
        WorkspaceUserRole wur = workspaceUserRoleRepository
                .findByWorkspaceIdAndUserIdAndRoleIdAndDeletedAtIsNull(workspaceId, userId, roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role assignment not found for user " + userId + " in workspace " + workspaceId));

        workspaceUserRoleRepository.delete(wur);
        log.info("Removed role {} from user {} in workspace {}", roleId, userId, workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRoleInWorkspace(Long workspaceId, Long userId, String roleName) {
        List<String> roles = workspaceUserRoleRepository.findRoleNamesByWorkspaceAndUser(workspaceId, userId);
        return roles.contains(roleName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermissionInWorkspace(Long workspaceId, Long userId, String permissionName) {
        List<String> permissions = workspaceUserRoleRepository.findPermissionNamesByWorkspaceAndUser(workspaceId, userId);
        return permissions.contains(permissionName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceUserRole> getAllByUserId(Long userId) {
        return workspaceUserRoleRepository.findByUserIdAndDeletedAtIsNull(userId);
    }
}
