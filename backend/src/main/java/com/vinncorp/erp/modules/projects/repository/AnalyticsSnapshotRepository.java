package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.AnalyticsSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, Long> {

    Page<AnalyticsSnapshot> findByWorkspaceIdAndSnapshotTypeAndDeletedAtIsNull(
            Long workspaceId, String snapshotType, Pageable pageable);

    Page<AnalyticsSnapshot> findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(
            Long workspaceId, Long projectId, Pageable pageable);
}



