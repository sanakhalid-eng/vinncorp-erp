package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {
    List<ApprovalWorkflow> findByWorkspaceIdAndIsActiveTrue(Long workspaceId);
}



