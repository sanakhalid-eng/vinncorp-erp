package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeArticleRequest {
    private Long projectId;
    @NotBlank
    private String title;
    private String markdownContent;
    private String tagsJson;
    private boolean published;
}



