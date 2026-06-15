package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CommandPaletteItemResponse;

import java.util.List;

public interface CommandPaletteService {

    List<CommandPaletteItemResponse> search(Long workspaceId, Long userId, String query);

    void recordRecent(Long workspaceId, Long userId, String actionKey, String actionLabel, String targetUrl);

    List<CommandPaletteItemResponse> getRecent(Long workspaceId, Long userId);
}



