package com.vinncorp.erp.platform.workspace.dto.request;

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

