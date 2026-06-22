package com.vinncorp.erp.shared.tenant;

import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantAccessValidator {

    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;

    public void validateWorkspaceAccess(Long workspaceId, Long userId) {
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId);
        if (!isMember) {
            throw new CustomAccessDeniedException("Cross-workspace access denied");
        }
    }

    public void validateProjectWorkspace(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        Long projectWorkspaceId = project.getWorkspace().getId();
        Long currentWorkspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (!projectWorkspaceId.equals(currentWorkspaceId)) {
            throw new CustomAccessDeniedException("Cross-workspace access denied");
        }
    }

    public void validateEntityWorkspace(TenantScopedEntity entity) {
        Long entityWorkspaceId = entity.getWorkspaceId();
        Long currentWorkspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (!entityWorkspaceId.equals(currentWorkspaceId)) {
            throw new CustomAccessDeniedException("Cross-workspace access denied");
        }
    }

    public void requireWorkspaceAccess(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId);
        if (!isMember) {
            throw new CustomAccessDeniedException("Cross-workspace access denied");
        }
    }
}



