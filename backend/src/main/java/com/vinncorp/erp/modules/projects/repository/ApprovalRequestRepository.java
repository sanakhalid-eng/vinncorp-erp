package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByWorkspaceIdAndStatus(Long workspaceId, String status);
    List<ApprovalRequest> findByRequesterId(Long requesterId);
    List<ApprovalRequest> findByWorkspaceIdAndEntityType(Long workspaceId, String entityType);
}



