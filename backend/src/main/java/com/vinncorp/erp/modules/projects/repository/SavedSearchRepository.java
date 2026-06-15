package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {

    List<SavedSearch> findByWorkspaceIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long workspaceId, Long userId);

    Optional<SavedSearch> findByIdAndWorkspaceIdAndUserIdAndDeletedAtIsNull(
            Long id, Long workspaceId, Long userId);
}



