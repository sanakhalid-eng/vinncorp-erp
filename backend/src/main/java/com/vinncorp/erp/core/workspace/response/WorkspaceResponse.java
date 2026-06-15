package com.vinncorp.erp.core.workspace.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private boolean active;
    private Long memberCount;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

