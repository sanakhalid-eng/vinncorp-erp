package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeArticleResponse {
    private Long id;
    private Long projectId;
    private String title;
    private String slug;
    private String markdownContent;
    private String tagsJson;
    private boolean published;
}



