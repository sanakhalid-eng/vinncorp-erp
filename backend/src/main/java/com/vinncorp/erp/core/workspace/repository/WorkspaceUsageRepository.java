package com.vinncorp.erp.core.workspace.repository;

import com.vinncorp.erp.core.workspace.entity.WorkspaceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceUsageRepository extends JpaRepository<WorkspaceUsage, Long> {
    Optional<WorkspaceUsage> findByWorkspaceId(Long workspaceId);
}

