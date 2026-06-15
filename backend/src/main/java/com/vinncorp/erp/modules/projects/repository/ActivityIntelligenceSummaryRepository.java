package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ActivityIntelligenceSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityIntelligenceSummaryRepository extends JpaRepository<ActivityIntelligenceSummary, Long> {

    Page<ActivityIntelligenceSummary> findByWorkspaceIdAndDeletedAtIsNull(
            Long workspaceId, Pageable pageable);

    Page<ActivityIntelligenceSummary> findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(
            Long workspaceId, Long projectId, Pageable pageable);

    Optional<ActivityIntelligenceSummary> findTopByWorkspaceIdAndProjectIdAndSummaryTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long workspaceId, Long projectId, String summaryType);
}



