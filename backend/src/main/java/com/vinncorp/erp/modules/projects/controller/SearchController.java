package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.entity.SearchResult;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search")
public class SearchController {

    private final SearchService searchService;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;

    @GetMapping
    @Operation(summary = "Global search across workspace")
    public ResponseEntity<ApiResponse<SearchResult>> search(
            @RequestParam String q,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        SearchResult result = searchService.search(q, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Search results", result));
    }
}



