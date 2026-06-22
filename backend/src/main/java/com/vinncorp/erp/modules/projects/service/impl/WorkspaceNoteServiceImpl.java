package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.platform.workspace.dto.request.WorkspaceNoteRequest;
import com.vinncorp.erp.platform.workspace.dto.response.WorkspaceNoteResponse;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.entity.WorkspaceNote;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceNoteRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.service.impl.WorkspaceNoteService;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceNoteServiceImpl implements WorkspaceNoteService {

    private final WorkspaceNoteRepository noteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public Page<WorkspaceNoteResponse> list(Long workspaceId, Long projectId, Pageable pageable) {
        requireWorkspace(workspaceId);
        if (projectId != null) {
            requireProjectInWorkspace(workspaceId, projectId);
            return noteRepository
                    .findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(workspaceId, projectId, pageable)
                    .map(this::toResponse);
        }
        return noteRepository.findByWorkspaceIdAndProjectIdIsNullAndDeletedAtIsNull(workspaceId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public WorkspaceNoteResponse create(Long workspaceId, Long userId, WorkspaceNoteRequest request) {
        Workspace workspace = requireWorkspace(workspaceId);
        if (request.getProjectId() != null) {
            requireProjectInWorkspace(workspaceId, request.getProjectId());
        }
        WorkspaceNote note = new WorkspaceNote();
        note.setWorkspace(workspace);
        note.setUserId(userId);
        note.setProjectId(request.getProjectId());
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setPinned(request.isPinned());
        WorkspaceNoteResponse response = toResponse(noteRepository.save(note));
        cacheService.evict(CacheNames.workspaceNotes(workspaceId));
        return response;
    }

    @Override
    @Transactional
    public WorkspaceNoteResponse update(Long workspaceId, Long id, WorkspaceNoteRequest request) {
        WorkspaceNote note = noteRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        if (request.getProjectId() != null) {
            requireProjectInWorkspace(workspaceId, request.getProjectId());
            note.setProjectId(request.getProjectId());
        }
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setPinned(request.isPinned());
        WorkspaceNoteResponse response = toResponse(noteRepository.save(note));
        cacheService.evict(CacheNames.workspaceNotes(workspaceId));
        return response;
    }

    @Override
    @Transactional
    public void delete(Long workspaceId, Long id) {
        WorkspaceNote note = noteRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        note.softDelete(null);
        noteRepository.save(note);
        cacheService.evict(CacheNames.workspaceNotes(workspaceId));
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

    private WorkspaceNoteResponse toResponse(WorkspaceNote note) {
        return WorkspaceNoteResponse.builder()
                .id(note.getId())
                .projectId(note.getProjectId())
                .userId(note.getUserId())
                .title(note.getTitle())
                .content(note.getContent())
                .pinned(note.isPinned())
                .build();
    }
}



