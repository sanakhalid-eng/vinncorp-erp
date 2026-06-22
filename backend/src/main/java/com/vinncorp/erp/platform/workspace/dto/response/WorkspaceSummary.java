package com.vinncorp.erp.platform.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceSummary {
    private Long id;
    private String name;
    private String slug;
    private String role;
}

