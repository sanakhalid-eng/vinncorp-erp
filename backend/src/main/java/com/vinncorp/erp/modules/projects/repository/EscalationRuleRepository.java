package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.EscalationRule;
import com.vinncorp.erp.modules.projects.enums.EscalationTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationRuleRepository extends JpaRepository<EscalationRule, Long> {

    List<EscalationRule> findByWorkspaceIdAndEnabledTrue(Long workspaceId);

    List<EscalationRule> findByProjectIdAndEnabledTrue(Long projectId);

    List<EscalationRule> findByWorkspaceId(Long workspaceId);

    List<EscalationRule> findByWorkspaceIdAndProjectId(Long workspaceId, Long projectId);

    List<EscalationRule> findByTriggerConditionAndEnabledTrue(EscalationTrigger triggerCondition);
}



