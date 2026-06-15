package com.vinncorp.erp.core.workspace.service.Impl;

import com.vinncorp.erp.core.workspace.request.WorkspaceNoteRequest;
import com.vinncorp.erp.core.workspace.response.WorkspaceNoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkspaceNoteService {

    Page<WorkspaceNoteResponse> list(Long workspaceId, Long projectId, Pageable pageable);

    WorkspaceNoteResponse create(Long workspaceId, Long userId, WorkspaceNoteRequest request);

    WorkspaceNoteResponse update(Long workspaceId, Long id, WorkspaceNoteRequest request);

    void delete(Long workspaceId, Long id);
}

