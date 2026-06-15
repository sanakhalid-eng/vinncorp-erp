package com.vinncorp.erp.core.workspace.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkspaceNoteRequest {
    private Long projectId;
    @NotBlank
    private String title;
    private String content;
    private boolean pinned;
}

