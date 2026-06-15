package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByRequestIdOrderByStepNumberAsc(Long requestId);
    List<ApprovalStep> findByApproverIdAndStatus(Long approverId, String status);
}



