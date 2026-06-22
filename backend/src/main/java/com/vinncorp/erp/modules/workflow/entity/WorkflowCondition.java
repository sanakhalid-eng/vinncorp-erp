package com.vinncorp.erp.modules.workflow.entity;
import com.vinncorp.erp.modules.workflow.enums.WorkflowConditionOperator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity
@Table(name = "workflow_conditions", indexes = {
@Index(name = "idx_wf_conditions_rule", columnList = "rule_id")}) @Getter
@Setter

public class WorkflowCondition {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "rule_id", nullable = false) private WorkflowRule rule;
@Column(name = "field_name", nullable = false, length = 100) private String fieldName;
@Enumerated(EnumType.STRING) @Column(name = "operator", nullable = false, length = 30) private WorkflowConditionOperator operator;
@Column(name = "comparison_value", nullable = false, length = 500) private String comparisonValue;
@Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
@PrePersist protected void onCreate() {
createdAt = LocalDateTime.now();
}} 