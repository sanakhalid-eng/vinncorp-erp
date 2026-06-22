package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.platform.user.repository.RoleRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceOwnerSecurity {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MembershipResolver membershipResolver;

    public boolean isWorkspaceOwner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        String email = auth.getName();
        return membershipResolver.isWorkspaceOwner(email);
    }

    public boolean canAssignAdminRole() {
        return isWorkspaceOwner();
    }

    public boolean isAssigningAdminRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        return role != null && "ADMIN".equals(role.getName());
    }
}


