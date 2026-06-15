package com.vinncorp.erp.core.user.entity;

import com.vinncorp.erp.modules.projects.entity.Role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.vinncorp.erp.core.audit.BaseAuditableEntity;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    @JsonIgnore
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserRole> userRoles;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "workspace_owner", nullable = false)
    private boolean workspaceOwner = false;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    public List<String> getRoles() {
        if (userRoles == null) return Collections.emptyList();

        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .toList();
    }

    public void addRole(Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(this);
        userRole.setRole(role);

        this.userRoles.add(userRole);
    }
}

