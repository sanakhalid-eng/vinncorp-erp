package com.vinncorp.erp.platform.workspace.repository;

import com.vinncorp.erp.platform.workspace.entity.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRoleRepository extends JpaRepository<WorkspaceRole, Long> {
    List<WorkspaceRole> findByWorkspaceId(Long workspaceId);
    List<WorkspaceRole> findByWorkspaceIdAndSystemManagedTrue(Long workspaceId);
    Optional<WorkspaceRole> findByWorkspaceIdAndName(Long workspaceId, String name);
}

