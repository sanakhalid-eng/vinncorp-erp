package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ExecutionRiskSnapshot;
import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutionRiskSnapshotRepository extends JpaRepository<ExecutionRiskSnapshot, Long> {

    Optional<ExecutionRiskSnapshot> findByWorkspaceIdAndProjectIdAndSprintIdAndDeletedAtIsNull(Long workspaceId, Long projectId, Long sprintId);

    Optional<ExecutionRiskSnapshot> findTopByWorkspaceIdAndProjectIdAndSprintIdOrderByCreatedAtDesc(Long workspaceId, Long projectId, Long sprintId);

    Optional<ExecutionRiskSnapshot> findTopByWorkspaceIdAndProjectIdOrderByCreatedAtDesc(Long workspaceId, Long projectId);

    List<ExecutionRiskSnapshot> findByWorkspaceIdAndRiskLevelAndDeletedAtIsNull(Long workspaceId, RiskLevel riskLevel);
}



