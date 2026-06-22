package com.vinncorp.erp.modules.workflow.entity;
import jakarta.persistence.*;
import lombok.Data;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
@Data
@Entity
@Table(name = "workflow_transitions") 
public class WorkflowTransition {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
@ManyToOne private WorkflowStatus fromStatus;
@ManyToOne private WorkflowStatus toStatus;
@ManyToOne private Project project;
@Column(columnDefinition = "TEXT") private String rule;
} 