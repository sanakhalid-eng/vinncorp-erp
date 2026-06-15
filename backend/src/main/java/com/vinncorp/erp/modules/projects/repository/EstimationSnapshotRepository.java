package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.EstimationSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstimationSnapshotRepository extends JpaRepository<EstimationSnapshot, Long> {

    List<EstimationSnapshot> findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(Long workspaceId, Long projectId);

    List<EstimationSnapshot> findByWorkspaceIdAndProjectIdAndTaskIdAndDeletedAtIsNull(Long workspaceId, Long projectId, Long taskId);

    Optional<EstimationSnapshot> findTopByWorkspaceIdAndProjectIdAndTaskIdOrderByCreatedAtDesc(Long workspaceId, Long projectId, Long taskId);

    Page<EstimationSnapshot> findByWorkspaceIdAndProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long workspaceId, Long projectId, Pageable pageable);
}



