package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/workspaces")
@RequiredArgsConstructor
@Tag(name = "Admin Workspace Management")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminWorkspaceController {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @GetMapping
    @Operation(summary = "List all workspaces (platform-wide)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAllWorkspaces() {
        List<Workspace> workspaces = workspaceRepository.findAll();
        List<Map<String, Object>> result = workspaces.stream().map(ws -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", ws.getId());
            item.put("name", ws.getName());
            item.put("slug", ws.getSlug());
            item.put("description", ws.getDescription());
            item.put("active", ws.isActive());
            item.put("memberCount", workspaceMemberRepository.countByWorkspaceIdAndActiveTrue(ws.getId()));
            item.put("createdAt", ws.getCreatedAt());
            return item;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success("Workspaces fetched", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace details (platform-level)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWorkspace(@PathVariable Long id) {
        Workspace ws = workspaceRepository.findById(id)
                .orElseThrow(() -> new com.vinncorp.erp.shared.exception.ResourceNotFoundException("Workspace not found: " + id));
        Map<String, Object> item = new HashMap<>();
        item.put("id", ws.getId());
        item.put("name", ws.getName());
        item.put("slug", ws.getSlug());
        item.put("description", ws.getDescription());
        item.put("active", ws.isActive());
        item.put("memberCount", workspaceMemberRepository.countByWorkspaceIdAndActiveTrue(ws.getId()));
        item.put("createdAt", ws.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success("Workspace fetched", item));
    }

    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "Activate or deactivate a workspace")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        Workspace ws = workspaceRepository.findById(id)
                .orElseThrow(() -> new com.vinncorp.erp.shared.exception.ResourceNotFoundException("Workspace not found: " + id));
        ws.setActive(!ws.isActive());
        workspaceRepository.save(ws);
        return ResponseEntity.ok(ApiResponse.success("Workspace " + (ws.isActive() ? "activated" : "deactivated")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a workspace (platform-level)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(@PathVariable Long id) {
        Workspace ws = workspaceRepository.findById(id)
                .orElseThrow(() -> new com.vinncorp.erp.shared.exception.ResourceNotFoundException("Workspace not found: " + id));
        workspaceRepository.delete(ws);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted"));
    }
}
