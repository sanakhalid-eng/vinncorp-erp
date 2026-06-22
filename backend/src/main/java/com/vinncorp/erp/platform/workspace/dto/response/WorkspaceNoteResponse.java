package com.vinncorp.erp.platform.workspace.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceNoteResponse {
    private Long id;
    private Long projectId;
    private Long userId;
    private String title;
    private String content;
    private boolean pinned;
}

