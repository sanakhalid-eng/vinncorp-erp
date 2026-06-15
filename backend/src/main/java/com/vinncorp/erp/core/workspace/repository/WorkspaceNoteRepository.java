package com.vinncorp.erp.core.workspace.repository;

import com.vinncorp.erp.core.workspace.entity.WorkspaceNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceNoteRepository extends JpaRepository<WorkspaceNote, Long> {

    Page<WorkspaceNote> findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(
            Long workspaceId, Long projectId, Pageable pageable);

    Page<WorkspaceNote> findByWorkspaceIdAndProjectIdIsNullAndDeletedAtIsNull(
            Long workspaceId, Pageable pageable);

    Optional<WorkspaceNote> findByIdAndWorkspaceIdAndDeletedAtIsNull(Long id, Long workspaceId);
}

