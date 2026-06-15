package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.entity.UserRole;
import com.vinncorp.erp.modules.projects.enums.RoleScope;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionSecurity {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public boolean hasPermission(Long projectId, String email, String permissionName) {
        if (email == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null) {
                return auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                     a.getAuthority().equals(permissionName));
            }
            return false;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<String> userRoles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());

        if (userRoles.contains("ADMIN")) {
            return true;
        }

        return permissionService.hasPermission(user.getId(), projectId, permissionName);
    }

    public boolean isAdmin(String email) {
        if (email == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null) {
                return auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }
            return false;
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) return false;

        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .anyMatch(r -> r.getName().equals("ADMIN"));
    }

    public boolean hasSystemRole(String email, String roleName) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) return false;

        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(r -> r.getScope() == RoleScope.SYSTEM)
                .anyMatch(r -> r.getName().equals(roleName));
    }
}




