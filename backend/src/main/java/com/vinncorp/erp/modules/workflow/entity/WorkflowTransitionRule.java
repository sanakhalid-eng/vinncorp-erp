package com.vinncorp.erp.modules.workflow.entity;
import jakarta.persistence.*;
import lombok.Data;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
@Entity
@Table(name = "workflow_transition_rules") @Data

public class WorkflowTransitionRule {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
@ManyToOne @JoinColumn(name = "project_id") private Project project;
@ManyToOne private WorkflowStatus fromStatus;
@ManyToOne private WorkflowStatus toStatus;
@Deprecated private String allowedRole;
@Column(name = "required_permissions", columnDefinition = "TEXT") private String requiredPermissions;
@Column(name = "rule_condition") private String condition;
@Column(columnDefinition = "TEXT") private String ruleJson;
} 