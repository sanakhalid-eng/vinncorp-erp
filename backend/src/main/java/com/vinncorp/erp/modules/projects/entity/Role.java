package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.RoleScope;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;
import com.vinncorp.erp.platform.user.entity.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "permissions")
@Data
public class Role extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleScope scope;

    @Column(name = "is_system_role", nullable = false)
    private boolean isSystemRole;

    @Column(name = "is_editable", nullable = false)
    private boolean isEditable = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}



