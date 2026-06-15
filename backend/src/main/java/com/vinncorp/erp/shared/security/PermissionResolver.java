package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.entity.UserRole;
import com.vinncorp.erp.modules.projects.enums.RoleScope;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionResolver {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final MembershipResolver membershipResolver;

    public boolean hasProjectPermission(Long projectId, String email, String permissionName) {
        if (email == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null) {
                return auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals(permissionName));
            }
            return false;
        }

        // SUPER_ADMIN bypass: has all permissions everywhere
        if (membershipResolver.isSuperAdmin(email)) return true;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        if (membershipResolver.isAdmin(user)) {
            return true;
        }

        return permissionService.hasPermission(user.getId(), projectId, permissionName);
    }

    public boolean hasSystemPermission(String email, String permissionName) {
        // SUPER_ADMIN bypass: has all permissions everywhere
        if (membershipResolver.isSuperAdmin(email)) return true;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        if (membershipResolver.isAdmin(user)) {
            return true;
        }

        return user.getUserRoles().stream()
                .flatMap(ur -> ur.getRole().getPermissions().stream())
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    public boolean hasSystemRole(String email, String roleName) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(r -> r.getScope() == RoleScope.SYSTEM)
                .anyMatch(r -> r.getName().equals(roleName));
    }

    public boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null && membershipResolver.isAdmin(user);
    }

    public boolean isSuperAdmin(String email) {
        return membershipResolver.isSuperAdmin(email);
    }
}
