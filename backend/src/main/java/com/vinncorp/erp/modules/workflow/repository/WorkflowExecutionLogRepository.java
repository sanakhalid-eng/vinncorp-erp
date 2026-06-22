package com.vinncorp.erp.modules.workflow.repository;
import com.vinncorp.erp.modules.workflow.entity.WorkflowExecutionLog;
import com.vinncorp.erp.modules.projects.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository

public interface WorkflowExecutionLogRepository extends JpaRepository<WorkflowExecutionLog, Long> {
List<WorkflowExecutionLog> findByRuleIdOrderByCreatedAtDesc(Long ruleId);
Page<WorkflowExecutionLog> findByRuleIdOrderByCreatedAtDesc(Long ruleId, Pageable pageable);
List<WorkflowExecutionLog> findByStatusOrderByCreatedAtDesc(ExecutionStatus status);
long countByRuleIdAndStatus(Long ruleId, ExecutionStatus status);
long countByStatus(ExecutionStatus status);
} 