package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Page<KnowledgeArticle> findByWorkspaceIdAndDeletedAtIsNull(Long workspaceId, Pageable pageable);

    Page<KnowledgeArticle> findByWorkspaceIdAndPublishedTrueAndDeletedAtIsNull(
            Long workspaceId, Pageable pageable);

    Optional<KnowledgeArticle> findByWorkspaceIdAndSlugAndDeletedAtIsNull(Long workspaceId, String slug);

    Optional<KnowledgeArticle> findByIdAndWorkspaceIdAndDeletedAtIsNull(Long id, Long workspaceId);
}



