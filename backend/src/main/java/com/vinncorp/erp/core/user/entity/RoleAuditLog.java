package com.vinncorp.erp.core.user.entity;

import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.modules.projects.entity.Project;

import com.vinncorp.erp.core.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;



@Entity
@Table(name = "role_audit_logs")
@Data
@EqualsAndHashCode(callSuper = false)
public class RoleAuditLog extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Role role;

    @ManyToOne
    private Project project;

    private String action;
}


