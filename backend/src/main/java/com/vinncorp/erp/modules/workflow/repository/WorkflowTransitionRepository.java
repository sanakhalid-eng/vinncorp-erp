package com.vinncorp.erp.modules.workflow.repository;
import com.vinncorp.erp.modules.workflow.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
List<WorkflowTransition> findByProjectId(Long projectId);
boolean existsByProjectIdAndFromStatusIdAndToStatusId(Long projectId, Long fromStatusId, Long toStatusId);
} 