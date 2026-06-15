package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.WorkflowTransitionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface WorkflowTransitionRuleRepository extends JpaRepository<WorkflowTransitionRule, Long> {

    boolean existsByProjectIdAndFromStatusIdAndToStatusIdAndAllowedRole(
            Long projectId,
            Long fromStatusId,
            Long toStatusId,
            String allowedRole
    );

    @Query("""
SELECT r.ruleJson 
FROM WorkflowTransitionRule r 
WHERE r.project.id = :projectId 
AND r.fromStatus.id = :fromId 
AND r.toStatus.id = :toId
""")
    Optional<String> findRuleJson(Long projectId, Long fromId, Long toId);

    @Query("""
SELECT r.requiredPermissions
FROM WorkflowTransitionRule r
WHERE r.project.id = :projectId
AND r.fromStatus.id = :fromId
AND r.toStatus.id = :toId
""")
    Optional<String> findRequiredPermissions(Long projectId, Long fromId, Long toId);
}



