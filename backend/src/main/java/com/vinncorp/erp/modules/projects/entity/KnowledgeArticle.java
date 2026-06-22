package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "knowledge_articles", indexes = {
        @Index(name = "idx_knowledge_ws_slug", columnList = "workspace_id, slug")
})
@Getter
@Setter
public class KnowledgeArticle extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String slug;

    @Column(name = "markdown_content", columnDefinition = "MEDIUMTEXT")
    private String markdownContent;

    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    @Column(nullable = false)
    private boolean published;
}



