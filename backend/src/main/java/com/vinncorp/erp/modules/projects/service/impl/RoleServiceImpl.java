package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.entity.Permission;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.PermissionRepository;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.user.repository.RoleRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.repository.UserRoleRepository;
import com.vinncorp.erp.platform.user.service.RoleService;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.enums.RoleScope;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public Role createRole(Role role) {
        if (role.getScope() == null) {
            role.setScope(RoleScope.PROJECT);
        }
        if (role.getName() == null || role.getName().isBlank()) {
            throw new BadRequestException("Role name is required");
        }
        if (!role.isEditable()) {
            throw new BadRequestException("New roles must be editable");
        }
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public Role updateRole(Long id, Role updated) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Non-editable roles cannot be modified
        if (!role.isEditable()) {
            // Audit log for blocked edit
            activityLogService.logActivity(
                    null, EntityType.ROLE, id, ActionType.ROLE_EDIT_BLOCKED,
                    Map.of("roleName", role.getName(), "reason", "Non-editable role"),
                    null, "Blocked attempt to edit non-editable role: " + role.getName(), null
            );
            throw new BadRequestException("Role " + role.getName() + " is not editable");
        }

        // System roles: prevent changing name or scope
        if (role.isSystemRole()) {
            if (updated.getName() != null && !updated.getName().equals(role.getName())) {
                activityLogService.logActivity(
                        null, EntityType.ROLE, id, ActionType.ROLE_EDIT_BLOCKED,
                        Map.of("roleName", role.getName(), "reason", "Cannot rename system role"),
                        null, "Blocked attempt to rename system role: " + role.getName(), null
                );
                throw new BadRequestException("Cannot rename a system role");
            }
            if (updated.getScope() != null && updated.getScope() != role.getScope()) {
                throw new BadRequestException("Cannot change the scope of a system role");
            }
        }

        // Prevent scope conversion
        if (updated.getScope() != null && updated.getScope() != role.getScope()) {
            throw new BadRequestException("Cannot convert a " + role.getScope() + " role to " + updated.getScope());
        }

        if (updated.getName() != null) {
            role.setName(updated.getName());
        }
        if (updated.getDescription() != null) {
            role.setDescription(updated.getDescription());
        }
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // System roles cannot be deleted
        if (role.isSystemRole()) {
            activityLogService.logActivity(
                    null, EntityType.ROLE, id, ActionType.ROLE_DELETE_BLOCKED,
                    Map.of("roleName", role.getName(), "reason", "System role"),
                    null, "Blocked attempt to delete system role: " + role.getName(), null
            );
            throw new BadRequestException("System role " + role.getName() + " cannot be deleted");
        }

        // Non-editable roles cannot be deleted
        if (!role.isEditable()) {
            activityLogService.logActivity(
                    null, EntityType.ROLE, id, ActionType.ROLE_DELETE_BLOCKED,
                    Map.of("roleName", role.getName(), "reason", "Non-editable role"),
                    null, "Blocked attempt to delete non-editable role: " + role.getName(), null
            );
            throw new BadRequestException("Role " + role.getName() + " is not editable and cannot be deleted");
        }

        // Prevent deleting roles in use
        boolean roleInUse = userRoleRepository.findByRoleId(id).size() > 0;
        if (roleInUse) {
            throw new BadRequestException("Role " + role.getName() + " is currently assigned to users and cannot be deleted");
        }

        Long deletedBy = getCurrentUserId();
        role.softDelete(deletedBy);
        roleRepository.save(role);
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String email = auth.getName();
            return userRepository.findByEmail(email).map(User::getId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public Role updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (!role.isEditable()) {
            throw new BadRequestException("Role " + role.getName() + " is not editable");
        }

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }
}



