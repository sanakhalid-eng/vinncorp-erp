package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.core.user.entity.UserRole;
import com.vinncorp.erp.core.user.entity.WorkspaceUserRole;
import com.vinncorp.erp.core.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.modules.projects.entity.BootstrapLock;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.modules.projects.repository.BootstrapLockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.vinncorp.erp.core.user.repository.RoleRepository;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.user.repository.WorkspaceUserRoleRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;

@Service
@RequiredArgsConstructor
public class BootstrapService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BootstrapLockRepository bootstrapLockRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceUserRoleRepository workspaceUserRoleRepository;

    @PostConstruct
    @Transactional
    public void initLock() {
        if (bootstrapLockRepository.count() == 0) {
            BootstrapLock lock = new BootstrapLock();
            lock.setLockName("BOOTSTRAP");
            bootstrapLockRepository.save(lock);
        }
    }

    @Transactional
    public User createFirstUser(User user) {
        bootstrapLockRepository.lockBootstrap()
                .orElseThrow(() -> new RuntimeException("Bootstrap lock not initialized"));

        if (userRepository.existsByWorkspaceOwnerTrue()) {
            throw new IllegalStateException("Workspace owner already exists");
        }

        user.setWorkspaceOwner(true);
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(adminRole);
        user.getUserRoles().add(userRole);

        User savedUser = userRepository.save(user);
        addToDefaultWorkspace(savedUser, "WORKSPACE_OWNER", adminRole);
        return savedUser;
    }

    @Transactional
    public User createSubsequentUser(User user) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        UserRole ur = new UserRole();
        ur.setUser(user);
        ur.setRole(userRole);
        user.getUserRoles().add(ur);
        user.setWorkspaceOwner(false);

        User savedUser = userRepository.save(user);
        addToDefaultWorkspace(savedUser, "WORKSPACE_MEMBER", userRole);
        return savedUser;
    }

    private void addToDefaultWorkspace(User user, String role, Role systemRole) {
        workspaceRepository.findBySlug("personal-workspace").ifPresent(ws -> {
            if (!workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(ws.getId(), user.getId())) {
                WorkspaceMember member = new WorkspaceMember();
                member.setWorkspace(ws);
                member.setUser(user);
                member.setWorkspaceRole(role);
                member.setJoinedAt(LocalDateTime.now());
                member.setActive(true);
                workspaceMemberRepository.save(member);

                // Also create workspace_user_roles entry for workspace-scoped RBAC
                if (!workspaceUserRoleRepository.existsByWorkspaceIdAndUserIdAndRoleIdAndDeletedAtIsNull(
                        ws.getId(), user.getId(), systemRole.getId())) {
                    WorkspaceUserRole wur = new WorkspaceUserRole();
                    wur.setWorkspace(ws);
                    wur.setUser(user);
                    wur.setRole(systemRole);
                    wur.setAssignedAt(LocalDateTime.now());
                    workspaceUserRoleRepository.save(wur);
                }
            }
        });
    }

    @Transactional(readOnly = true)
    public boolean isWorkspaceOwnerExists() {
        return userRepository.existsByWorkspaceOwnerTrue();
    }

    @Transactional
    public User assignAdminRole(User targetUser) {
        bootstrapLockRepository.lockBootstrap()
                .orElseThrow(() -> new RuntimeException("Bootstrap lock not initialized"));

        if (userRepository.existsByWorkspaceOwnerTrue()) {
            targetUser.setWorkspaceOwner(false);
        } else {
            targetUser.setWorkspaceOwner(true);
        }

        return userRepository.save(targetUser);
    }
}

