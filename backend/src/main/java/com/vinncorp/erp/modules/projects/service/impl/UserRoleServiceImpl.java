package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.service.UserRoleService;
import com.vinncorp.erp.platform.user.repository.UserRoleRepository;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.entity.UserRole;
import com.vinncorp.erp.platform.user.repository.RoleRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;


import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.security.MembershipResolver;
import com.vinncorp.erp.shared.security.WorkspaceOwnerSecurity;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.enums.RoleScope;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WorkspaceOwnerSecurity workspaceOwnerSecurity;
    private final MembershipResolver membershipResolver;
    private final ActivityLogService activityLogService;

    @Transactional
    public void assignSystemRole(Long userId, Long roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (role.getScope() != RoleScope.SYSTEM) {
            throw new BadRequestException("Only SYSTEM roles allowed here");
        }

        // Only workspaceOwner can assign ADMIN role
        if ("ADMIN".equals(role.getName()) && !workspaceOwnerSecurity.canAssignAdminRole()) {
            throw new CustomAccessDeniedException("Only the workspace owner can assign the ADMIN role");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Delete any existing SYSTEM roles for this user (role reassignment)
        List<UserRole> existingSystemRoles = userRoleRepository.findByUserId(userId).stream()
                .filter(ur -> ur.getRole().getScope() == RoleScope.SYSTEM)
                .collect(Collectors.toList());

        String previousRole = null;
        if (!existingSystemRoles.isEmpty()) {
            previousRole = existingSystemRoles.get(0).getRole().getName();
            userRoleRepository.deleteAll(existingSystemRoles);
            userRoleRepository.flush();
        }

        // Check if user already has this specific role
        boolean exists = userRoleRepository.existsByUserIdAndRoleId(userId, roleId);
        if (exists) {
            throw new BadRequestException("User already has this role");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(LocalDateTime.now());

        userRoleRepository.save(userRole);

        // Audit log for admin role changes
        if ("ADMIN".equals(role.getName())) {
            activityLogService.logActivity(
                    null,
                    EntityType.ROLE,
                    roleId,
                    ActionType.ADMIN_ASSIGNED,
                    previousRole != null ? Map.of("previousRole", previousRole) : null,
                    Map.of("assignedRole", role.getName(), "targetUserId", userId, "targetUserEmail", user.getEmail()),
                    "ADMIN role assigned to user " + user.getEmail(),
                    null
            );
        }
    }

    @Override
    @Transactional
    public void removeSystemRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (role.getScope() != RoleScope.SYSTEM) {
            throw new BadRequestException("Only SYSTEM roles can be removed here");
        }

        // Only workspaceOwner can remove ADMIN role
        if ("ADMIN".equals(role.getName()) && !workspaceOwnerSecurity.canAssignAdminRole()) {
            activityLogService.logActivity(
                    null, EntityType.ROLE, roleId, ActionType.ADMIN_ASSIGNMENT_BLOCKED,
                    Map.of("targetUserId", userId, "targetUserEmail", user.getEmail(), "reason", "Not workspace owner"),
                    null, "Blocked attempt to remove ADMIN role by non-owner", null
            );
            throw new CustomAccessDeniedException("Only the workspace owner can remove the ADMIN role");
        }

        // Prevent removing ADMIN from workspaceOwner
        if ("ADMIN".equals(role.getName()) && membershipResolver.isWorkspaceOwner(user)) {
            activityLogService.logActivity(
                    null, EntityType.ROLE, roleId, ActionType.ADMIN_ASSIGNMENT_BLOCKED,
                    Map.of("targetUserId", userId, "targetUserEmail", user.getEmail(), "reason", "Cannot remove ADMIN from workspace owner"),
                    null, "Blocked attempt to remove ADMIN role from workspace owner", null
            );
            throw new BadRequestException("Cannot remove the ADMIN role from the workspace owner");
        }

        // Prevent last admin removal
        if ("ADMIN".equals(role.getName())) {
            long adminCount = userRoleRepository.findAll().stream()
                    .filter(ur -> "ADMIN".equals(ur.getRole().getName()))
                    .count();
            if (adminCount <= 1) {
                activityLogService.logActivity(
                        null, EntityType.ROLE, roleId, ActionType.ADMIN_ASSIGNMENT_BLOCKED,
                        Map.of("targetUserId", userId, "targetUserEmail", user.getEmail(), "reason", "Last ADMIN"),
                        null, "Blocked attempt to remove the last ADMIN role", null
                );
                throw new BadRequestException("Cannot remove the last ADMIN role from the system");
            }
        }

        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        UserRole userRole = userRoles.stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User does not have this role"));

        userRoleRepository.delete(userRole);

        // Audit log for admin role changes
        if ("ADMIN".equals(role.getName())) {
            activityLogService.logActivity(
                    null,
                    EntityType.ROLE,
                    roleId,
                    ActionType.ADMIN_REMOVED,
                    Map.of("removedRole", role.getName(), "targetUserId", userId, "targetUserEmail", user.getEmail()),
                    null,
                    "ADMIN role removed from user " + user.getEmail(),
                    null
            );
        }
    }

    @Override
    public List<UserRole> getUserSystemRoles(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .filter(ur -> ur.getRole().getScope() == RoleScope.SYSTEM)
                .collect(Collectors.toList());
    }
}



