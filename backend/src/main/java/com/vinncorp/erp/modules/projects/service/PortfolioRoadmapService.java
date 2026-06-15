package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.PortfolioRoadmapItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioRoadmapService {

    Page<PortfolioRoadmapItemResponse> listByWorkspace(Long workspaceId, Pageable pageable);

    List<PortfolioRoadmapItemResponse> listByProject(Long workspaceId, Long projectId);

    PortfolioRoadmapItemResponse create(Long workspaceId, Long projectId, String title, String description,
                                        LocalDate milestoneDate, String status, Integer sortOrder);

    PortfolioRoadmapItemResponse update(Long workspaceId, Long itemId, String title, String description,
                                        LocalDate milestoneDate, String status, Integer sortOrder);

    void delete(Long workspaceId, Long itemId);
}



