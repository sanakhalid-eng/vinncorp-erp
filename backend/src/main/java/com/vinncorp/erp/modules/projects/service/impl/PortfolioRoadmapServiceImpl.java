package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.response.PortfolioRoadmapItemResponse;
import com.vinncorp.erp.modules.projects.entity.PortfolioRoadmapItem;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.repository.PortfolioRoadmapItemRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.PortfolioRoadmapService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioRoadmapServiceImpl implements PortfolioRoadmapService {

    private final PortfolioRoadmapItemRepository roadmapRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PortfolioRoadmapItemResponse> listByWorkspace(Long workspaceId, Pageable pageable) {
        requireWorkspace(workspaceId);
        return roadmapRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioRoadmapItemResponse> listByProject(Long workspaceId, Long projectId) {
        requireProjectInWorkspace(workspaceId, projectId);
        return roadmapRepository
                .findByWorkspaceIdAndProjectIdAndDeletedAtIsNullOrderBySortOrderAsc(workspaceId, projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PortfolioRoadmapItemResponse create(Long workspaceId, Long projectId, String title,
                                               String description, LocalDate milestoneDate,
                                               String status, Integer sortOrder) {
        Workspace workspace = requireWorkspace(workspaceId);
        requireProjectInWorkspace(workspaceId, projectId);

        PortfolioRoadmapItem item = new PortfolioRoadmapItem();
        item.setWorkspace(workspace);
        item.setProjectId(projectId);
        item.setTitle(title);
        item.setDescription(description);
        item.setMilestoneDate(milestoneDate);
        item.setStatus(status != null ? status : "PLANNED");
        item.setSortOrder(sortOrder != null ? sortOrder : 0);
        return toResponse(roadmapRepository.save(item));
    }

    @Override
    @Transactional
    public PortfolioRoadmapItemResponse update(Long workspaceId, Long itemId, String title,
                                               String description, LocalDate milestoneDate,
                                               String status, Integer sortOrder) {
        PortfolioRoadmapItem item = roadmapRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(itemId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap item not found"));
        if (title != null) item.setTitle(title);
        if (description != null) item.setDescription(description);
        if (milestoneDate != null) item.setMilestoneDate(milestoneDate);
        if (status != null) item.setStatus(status);
        if (sortOrder != null) item.setSortOrder(sortOrder);
        return toResponse(roadmapRepository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long workspaceId, Long itemId) {
        PortfolioRoadmapItem item = roadmapRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(itemId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap item not found"));
        item.softDelete(null);
        roadmapRepository.save(item);
    }

    private Workspace requireWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    private void requireProjectInWorkspace(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!workspaceId.equals(project.getWorkspace().getId())) {
            throw new ResourceNotFoundException("Project not found");
        }
    }

    private PortfolioRoadmapItemResponse toResponse(PortfolioRoadmapItem item) {
        return PortfolioRoadmapItemResponse.builder()
                .id(item.getId())
                .projectId(item.getProjectId())
                .title(item.getTitle())
                .description(item.getDescription())
                .milestoneDate(item.getMilestoneDate())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .build();
    }
}



