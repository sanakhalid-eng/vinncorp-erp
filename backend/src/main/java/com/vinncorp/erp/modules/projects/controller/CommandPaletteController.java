package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.CommandPaletteItemResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.service.CommandPaletteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/command-palette")
@RequiredArgsConstructor
@Tag(name = "Command Palette")
public class CommandPaletteController {

    private final CommandPaletteService commandPaletteService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    @Operation(summary = "Search command palette actions")
    public ResponseEntity<ApiResponse<List<CommandPaletteItemResponse>>> search(
            @RequestParam(defaultValue = "") String q,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Commands fetched",
                commandPaletteService.search(wsId, userDetails.user().getId(), q)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<CommandPaletteItemResponse>>> recent(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(ApiResponse.success("Recent commands fetched",
                commandPaletteService.getRecent(wsId, userDetails.user().getId())));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/recent")
    public ResponseEntity<ApiResponse<Void>> recordRecent(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        commandPaletteService.recordRecent(
                wsId,
                userDetails.user().getId(),
                body.get("actionKey"),
                body.get("actionLabel"),
                body.get("targetUrl"));
        return ResponseEntity.ok(ApiResponse.success("Command recorded", null));
    }
}



