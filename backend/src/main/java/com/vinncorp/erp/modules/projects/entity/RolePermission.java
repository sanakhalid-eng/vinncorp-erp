package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.user.entity.Permission;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_role_id", "permission_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_role_id", nullable = false)
    private ProjectRole projectRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}



