package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.request.KnowledgeArticleRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.KnowledgeArticleResponse;
import com.vinncorp.erp.modules.projects.service.KnowledgeHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge Hub")
public class KnowledgeHubController {

    private final KnowledgeHubService knowledgeHubService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "List knowledge articles")
    public ResponseEntity<ApiResponse<Page<KnowledgeArticleResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Articles fetched",
                knowledgeHubService.list(wsId, PageRequest.of(page, size))));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Page<KnowledgeArticleResponse>>> listPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Published articles fetched",
                knowledgeHubService.listPublished(wsId, PageRequest.of(page, size))));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> getBySlug(@PathVariable String slug) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Article fetched",
                knowledgeHubService.getBySlug(wsId, slug)));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> create(
            @Valid @RequestBody KnowledgeArticleRequest request) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Article created",
                knowledgeHubService.create(wsId, request)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeArticleRequest request) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Article updated",
                knowledgeHubService.update(wsId, id, request)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        knowledgeHubService.delete(wsId, id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted", null));
    }
}



