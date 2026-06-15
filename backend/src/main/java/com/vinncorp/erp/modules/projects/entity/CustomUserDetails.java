package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.entity.UserRole;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CustomUserDetails(
        User user,
        List<String> workspacePermissions,
        List<String> workspaceRoles,
        List<String> globalRoles
) implements UserDetails {

    /**
     * Legacy constructor for backward compatibility (uses global user_roles).
     */
    public CustomUserDetails(User user) {
        this(user, null, null, null);
    }

    /**
     * Constructor without globalRoles for backward compatibility.
     */
    public CustomUserDetails(User user, List<String> workspacePermissions, List<String> workspaceRoles) {
        this(user, workspacePermissions, workspaceRoles, null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Build authorities from global roles, workspace roles, and permissions
        Stream<String> roleStream = Stream.empty();

        // Add global roles as authorities (e.g., ROLE_SUPER_ADMIN)
        if (globalRoles != null) {
            roleStream = Stream.concat(roleStream,
                globalRoles.stream().map(role -> "ROLE_" + role));
        }

        // Add workspace roles as authorities (e.g., ROLE_WORKSPACE_ADMIN)
        if (workspaceRoles != null) {
            roleStream = Stream.concat(roleStream,
                workspaceRoles.stream().map(role -> "ROLE_" + role));
        }

        // If workspace-specific permissions are provided, use them
        if (workspacePermissions != null && !workspacePermissions.isEmpty()) {
            Stream<String> permStream = workspacePermissions.stream();
            return Stream.concat(roleStream, permStream)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }

        // Fallback: use global user_roles
        if (user.getUserRoles() == null) {
            return roleStream
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }

        Stream<String> fallbackRolePerms = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .flatMap(role -> Stream.concat(
                        Stream.of("ROLE_" + role.getName()),
                        role.getPermissions().stream()
                                .map(p -> p.getName())
                ));

        return Stream.concat(roleStream, fallbackRolePerms)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @NonNull
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
