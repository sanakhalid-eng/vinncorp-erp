package com.vinncorp.erp.modules.workflow.repository;
import com.vinncorp.erp.modules.workflow.entity.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatus, Long> {
List<WorkflowStatus> findByProjectId(Long projectId);
Optional<WorkflowStatus> findByProjectIdAndName(Long projectId, String name);
Optional<WorkflowStatus> findByIsDefaultTrueAndProjectId(Long projectId);
List<WorkflowStatus> findByProjectIdOrderByOrderIndexAsc(Long projectId);
} 