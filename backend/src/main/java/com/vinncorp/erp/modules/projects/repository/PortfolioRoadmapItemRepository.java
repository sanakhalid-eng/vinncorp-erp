package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.PortfolioRoadmapItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRoadmapItemRepository extends JpaRepository<PortfolioRoadmapItem, Long> {

    Page<PortfolioRoadmapItem> findByWorkspaceIdAndDeletedAtIsNull(Long workspaceId, Pageable pageable);

    List<PortfolioRoadmapItem> findByWorkspaceIdAndProjectIdAndDeletedAtIsNullOrderBySortOrderAsc(
            Long workspaceId, Long projectId);

    Optional<PortfolioRoadmapItem> findByIdAndWorkspaceIdAndDeletedAtIsNull(Long id, Long workspaceId);
}



