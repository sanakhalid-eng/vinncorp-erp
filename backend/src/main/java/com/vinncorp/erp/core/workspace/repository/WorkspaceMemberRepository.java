package com.vinncorp.erp.core.workspace.repository;

import com.vinncorp.erp.core.workspace.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);
    List<WorkspaceMember> findByWorkspaceIdIn(List<Long> workspaceIds);
    List<WorkspaceMember> findByWorkspaceIdAndActiveTrue(Long workspaceId);
    List<WorkspaceMember> findByUserIdAndActiveTrue(Long userId);
    Optional<WorkspaceMember> findByWorkspaceIdAndUserIdAndActiveTrue(Long workspaceId, Long userId);
    boolean existsByWorkspaceIdAndUserIdAndActiveTrue(Long workspaceId, Long userId);
    long countByWorkspaceIdAndActiveTrue(Long workspaceId);
}

