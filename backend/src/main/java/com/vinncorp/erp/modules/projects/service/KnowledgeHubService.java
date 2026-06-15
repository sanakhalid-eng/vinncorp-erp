package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.KnowledgeArticleRequest;
import com.vinncorp.erp.modules.projects.dto.response.KnowledgeArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KnowledgeHubService {

    Page<KnowledgeArticleResponse> list(Long workspaceId, Pageable pageable);

    Page<KnowledgeArticleResponse> listPublished(Long workspaceId, Pageable pageable);

    KnowledgeArticleResponse getBySlug(Long workspaceId, String slug);

    KnowledgeArticleResponse create(Long workspaceId, KnowledgeArticleRequest request);

    KnowledgeArticleResponse update(Long workspaceId, Long id, KnowledgeArticleRequest request);

    void delete(Long workspaceId, Long id);
}



