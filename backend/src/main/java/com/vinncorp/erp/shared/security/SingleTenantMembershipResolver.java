package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.user.repository.UserGlobalRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SingleTenantMembershipResolver implements MembershipResolver {

    private final UserRepository userRepository;
    private final UserGlobalRoleRepository userGlobalRoleRepository;

    @Override
    public boolean isWorkspaceOwner(User user) {
        return user != null && user.isWorkspaceOwner();
    }

    @Override
    public boolean isWorkspaceOwner(String email) {
        if (email == null) return false;
        return userRepository.findByEmail(email)
                .map(User::isWorkspaceOwner)
                .orElse(false);
    }

    @Override
    public boolean isWorkspaceOwner(Long userId) {
        if (userId == null) return false;
        return userRepository.findById(userId)
                .map(User::isWorkspaceOwner)
                .orElse(false);
    }

    @Override
    public User resolveWorkspaceOwner() {
        return userRepository.findByWorkspaceOwnerTrue()
                .orElse(null);
    }

    @Override
    public Long resolveWorkspaceOwnerId() {
        return userRepository.findByWorkspaceOwnerTrue()
                .map(User::getId)
                .orElse(null);
    }

    @Override
    public boolean canAssignAdminRole(User currentUser) {
        return isWorkspaceOwner(currentUser);
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null || user.getUserRoles() == null) return false;
        return user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName().equals("ADMIN"));
    }

    @Override
    public boolean isSuperAdmin(User user) {
        if (user == null) return false;
        return userGlobalRoleRepository.existsByUserIdAndGlobalRoleName(user.getId(), "SUPER_ADMIN");
    }

    @Override
    public boolean isSuperAdmin(String email) {
        if (email == null) return false;
        return userRepository.findByEmail(email)
                .map(this::isSuperAdmin)
                .orElse(false);
    }
}

