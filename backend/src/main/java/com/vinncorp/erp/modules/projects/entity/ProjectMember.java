package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.vinncorp.erp.platform.user.entity.User;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_id"}))
@Data
public class ProjectMember extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Deprecated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_role_id")
    private ProjectRole projectRole;

    @Column(nullable = false)
    private Boolean isActive = true;

    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public Long getRoleId() {
        return role != null ? role.getId() : null;
    }

    public Long getProjectRoleId() {
        return projectRole != null ? projectRole.getId() : null;
    }
}



