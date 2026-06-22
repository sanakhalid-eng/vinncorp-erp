package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.response.CommandPaletteItemResponse;
import com.vinncorp.erp.modules.projects.entity.CommandPaletteRecent;
import com.vinncorp.erp.modules.projects.repository.CommandPaletteRecentRepository;
import com.vinncorp.erp.modules.projects.service.CommandPaletteService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandPaletteServiceImpl implements CommandPaletteService {

    private static final List<CommandPaletteItemResponse> COMMANDS = List.of(
            item("create-task", "Create Task", "Tasks", "/tasks/new", "Ctrl+Shift+T"),
            item("create-project", "Create Project", "Projects", "/projects/new", "Ctrl+Shift+P"),
            item("go-dashboard", "Go to Dashboard", "Navigation", "/dashboard", "G D"),
            item("go-analytics", "Go to Analytics", "Navigation", "/analytics", "G A"),
            item("go-settings", "Workspace Settings", "Settings", "/settings", "G S"),
            item("search-tasks", "Search Tasks", "Search", "/search", "Ctrl+K"),
            item("sprint-board", "Open Sprint Board", "Sprints", "/sprints", null),
            item("invite-member", "Invite Member", "Team", "/invitations", null)
    );

    private final CommandPaletteRecentRepository recentRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommandPaletteItemResponse> search(Long workspaceId, Long userId, String query) {
        requireWorkspace(workspaceId);
        if (query == null || query.isBlank()) {
            return new ArrayList<>(COMMANDS);
        }
        String q = query.toLowerCase(Locale.ROOT);
        return COMMANDS.stream()
                .filter(c -> c.getLabel().toLowerCase(Locale.ROOT).contains(q)
                        || c.getCategory().toLowerCase(Locale.ROOT).contains(q)
                        || c.getKey().contains(q))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordRecent(Long workspaceId, Long userId, String actionKey,
                             String actionLabel, String targetUrl) {
        Workspace workspace = requireWorkspace(workspaceId);
        CommandPaletteRecent recent = new CommandPaletteRecent();
        recent.setWorkspace(workspace);
        recent.setUserId(userId);
        recent.setActionKey(actionKey);
        recent.setActionLabel(actionLabel);
        recent.setTargetUrl(targetUrl);
        recentRepository.save(recent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandPaletteItemResponse> getRecent(Long workspaceId, Long userId) {
        requireWorkspace(workspaceId);
        return recentRepository
                .findTop10ByWorkspaceIdAndUserIdAndDeletedAtIsNullOrderByUsedAtDesc(workspaceId, userId)
                .stream()
                .map(r -> CommandPaletteItemResponse.builder()
                        .key(r.getActionKey())
                        .label(r.getActionLabel())
                        .category("Recent")
                        .targetUrl(r.getTargetUrl())
                        .shortcut(null)
                        .build())
                .collect(Collectors.toList());
    }

    private static CommandPaletteItemResponse item(String key, String label, String category,
                                                   String url, String shortcut) {
        return CommandPaletteItemResponse.builder()
                .key(key).label(label).category(category).targetUrl(url).shortcut(shortcut)
                .build();
    }

    private Workspace requireWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }
}



