package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.workspace.entity.Workspace;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_workflows")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ApprovalWorkflow extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "trigger_condition", columnDefinition = "JSON")
    private String triggerCondition;

    @Column(name = "approval_chain", columnDefinition = "JSON", nullable = false)
    private String approvalChain;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}



