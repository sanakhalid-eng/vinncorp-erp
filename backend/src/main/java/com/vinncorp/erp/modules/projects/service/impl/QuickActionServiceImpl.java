package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.QuickActionResponse;
import com.vinncorp.erp.modules.projects.service.QuickActionService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuickActionServiceImpl implements QuickActionService {

    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<QuickActionResponse> listForWorkspace(Long workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        return List.of(
                QuickActionResponse.builder()
                        .key("create-task").label("Create Task").method("POST")
                        .path("/api/workspaces/" + workspaceId + "/tasks").icon("plus").build(),
                QuickActionResponse.builder()
                        .key("create-project").label("Create Project").method("POST")
                        .path("/api/workspaces/" + workspaceId + "/projects").icon("folder-plus").build(),
                QuickActionResponse.builder()
                        .key("invite-member").label("Invite Member").method("POST")
                        .path("/api/workspaces/" + workspaceId + "/invitations").icon("user-plus").build(),
                QuickActionResponse.builder()
                        .key("view-analytics").label("View Analytics").method("GET")
                        .path("/api/workspaces/" + workspaceId + "/analytics").icon("chart-bar").build(),
                QuickActionResponse.builder()
                        .key("sprint-planning").label("Sprint Planning").method("GET")
                        .path("/api/workspaces/" + workspaceId + "/sprint-planning").icon("calendar").build()
        );
    }
}



