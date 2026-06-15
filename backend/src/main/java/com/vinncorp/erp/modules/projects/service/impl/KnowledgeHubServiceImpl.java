package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.request.KnowledgeArticleRequest;
import com.vinncorp.erp.modules.projects.dto.response.KnowledgeArticleResponse;
import com.vinncorp.erp.modules.projects.entity.KnowledgeArticle;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.repository.KnowledgeArticleRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.KnowledgeHubService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class KnowledgeHubServiceImpl implements KnowledgeHubService {

    private final KnowledgeArticleRepository articleRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeArticleResponse> list(Long workspaceId, Pageable pageable) {
        requireWorkspace(workspaceId);
        return articleRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeArticleResponse> listPublished(Long workspaceId, Pageable pageable) {
        requireWorkspace(workspaceId);
        return articleRepository.findByWorkspaceIdAndPublishedTrueAndDeletedAtIsNull(workspaceId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeArticleResponse getBySlug(Long workspaceId, String slug) {
        KnowledgeArticle article = articleRepository
                .findByWorkspaceIdAndSlugAndDeletedAtIsNull(workspaceId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        return toResponse(article);
    }

    @Override
    @Transactional
    public KnowledgeArticleResponse create(Long workspaceId, KnowledgeArticleRequest request) {
        Workspace workspace = requireWorkspace(workspaceId);
        if (request.getProjectId() != null) {
            requireProjectInWorkspace(workspaceId, request.getProjectId());
        }
        KnowledgeArticle article = new KnowledgeArticle();
        article.setWorkspace(workspace);
        article.setProjectId(request.getProjectId());
        article.setTitle(request.getTitle());
        article.setSlug(resolveUniqueSlug(workspaceId, request.getTitle()));
        article.setMarkdownContent(request.getMarkdownContent());
        article.setTagsJson(request.getTagsJson());
        article.setPublished(request.isPublished());
        KnowledgeArticleResponse response = toResponse(articleRepository.save(article));
        cacheService.evict(CacheNames.knowledge(workspaceId));
        return response;
    }

    @Override
    @Transactional
    public KnowledgeArticleResponse update(Long workspaceId, Long id, KnowledgeArticleRequest request) {
        KnowledgeArticle article = articleRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        if (request.getProjectId() != null) {
            requireProjectInWorkspace(workspaceId, request.getProjectId());
            article.setProjectId(request.getProjectId());
        }
        if (request.getTitle() != null && !request.getTitle().equals(article.getTitle())) {
            article.setTitle(request.getTitle());
            article.setSlug(resolveUniqueSlug(workspaceId, request.getTitle()));
        }
        article.setMarkdownContent(request.getMarkdownContent());
        article.setTagsJson(request.getTagsJson());
        article.setPublished(request.isPublished());
        KnowledgeArticleResponse response = toResponse(articleRepository.save(article));
        cacheService.evict(CacheNames.knowledge(workspaceId));
        return response;
    }

    @Override
    @Transactional
    public void delete(Long workspaceId, Long id) {
        KnowledgeArticle article = articleRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.softDelete(null);
        articleRepository.save(article);
        cacheService.evict(CacheNames.knowledge(workspaceId));
    }

    private String resolveUniqueSlug(Long workspaceId, String title) {
        String base = toSlug(title);
        String candidate = base;
        int counter = 1;
        while (articleRepository.findByWorkspaceIdAndSlugAndDeletedAtIsNull(workspaceId, candidate).isPresent()) {
            candidate = base + "-" + counter++;
        }
        return candidate;
    }

    private String toSlug(String title) {
        if (title == null || title.isBlank()) return "article";
        return title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private Workspace requireWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    private void requireProjectInWorkspace(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!workspaceId.equals(project.getWorkspace().getId())) {
            throw new ResourceNotFoundException("Project not found");
        }
    }

    private KnowledgeArticleResponse toResponse(KnowledgeArticle article) {
        return KnowledgeArticleResponse.builder()
                .id(article.getId())
                .projectId(article.getProjectId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .markdownContent(article.getMarkdownContent())
                .tagsJson(article.getTagsJson())
                .published(article.isPublished())
                .build();
    }
}



