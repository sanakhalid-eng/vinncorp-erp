package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.PortfolioRoadmapItemResponse;
import com.vinncorp.erp.modules.projects.service.PortfolioRoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
@Tag(name = "Portfolio Roadmap")
public class PortfolioRoadmapController {

    private final PortfolioRoadmapService portfolioRoadmapService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List workspace roadmap items")
    public ResponseEntity<ApiResponse<Page<PortfolioRoadmapItemResponse>>> listWorkspace(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Roadmap fetched",
                portfolioRoadmapService.listByWorkspace(wsId, PageRequest.of(page, size))));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<List<PortfolioRoadmapItemResponse>>> listProject(@PathVariable Long projectId) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Project roadmap fetched",
                portfolioRoadmapService.listByProject(wsId, projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<PortfolioRoadmapItemResponse>> create(
            @PathVariable Long projectId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDate milestoneDate,
            @RequestParam(defaultValue = "PLANNED") String status,
            @RequestParam(required = false) Integer sortOrder) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Roadmap item created",
                portfolioRoadmapService.create(wsId, projectId, title, description, milestoneDate, status, sortOrder)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<PortfolioRoadmapItemResponse>> update(
            @PathVariable Long itemId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDate milestoneDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer sortOrder) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Roadmap item updated",
                portfolioRoadmapService.update(wsId, itemId, title, description, milestoneDate, status, sortOrder)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long itemId) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        portfolioRoadmapService.delete(wsId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Roadmap item deleted", null));
    }
}



