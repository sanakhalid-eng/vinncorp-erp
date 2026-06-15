package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.CriticalPathSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriticalPathSnapshotRepository extends JpaRepository<CriticalPathSnapshot, Long> {

    List<CriticalPathSnapshot> findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(Long workspaceId, Long projectId);

    List<CriticalPathSnapshot> findByWorkspaceIdAndProjectIdAndIsOnCriticalPathTrueAndDeletedAtIsNull(Long workspaceId, Long projectId);

    List<CriticalPathSnapshot> findByWorkspaceIdAndProjectIdAndTaskIdAndDeletedAtIsNull(Long workspaceId, Long projectId, Long taskId);
}



