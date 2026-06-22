package com.vinncorp.erp.modules.workflow.repository;
import com.vinncorp.erp.modules.workflow.engine.WorkflowTrigger;
import com.vinncorp.erp.modules.workflow.entity.WorkflowRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository

public interface WorkflowRuleRepository extends JpaRepository<WorkflowRule, Long> {
List<WorkflowRule> findByWorkspaceIdAndEnabledTrue(Long workspaceId);
List<WorkflowRule> findByProjectIdAndEnabledTrue(Long projectId);
@Query("SELECT r FROM WorkflowRule r WHERE r.enabled = true AND (r.projectId IS NULL OR r.projectId = :projectId) AND r.triggerType = :trigger ORDER BY r.executionOrder ASC") List<WorkflowRule> findActiveRulesByTrigger(
@Param("projectId") Long projectId, @Param("trigger") WorkflowTrigger trigger);
@Query("SELECT r FROM WorkflowRule r WHERE r.enabled = true AND r.workspaceId = :workspaceId AND (r.projectId IS NULL OR r.projectId = :projectId) AND r.triggerType = :trigger ORDER BY r.executionOrder ASC") List<WorkflowRule> findActiveRulesByTriggerAndWorkspace(
@Param("workspaceId") Long workspaceId, @Param("projectId") Long projectId, @Param("trigger") WorkflowTrigger trigger);
List<WorkflowRule> findByWorkspaceId(Long workspaceId);
List<WorkflowRule> findByWorkspaceIdAndProjectId(Long workspaceId, Long projectId);
} 