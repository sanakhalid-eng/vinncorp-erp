package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ProductivitySnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductivitySnapshotRepository extends JpaRepository<ProductivitySnapshot, Long> {

    Optional<ProductivitySnapshot> findByWorkspaceIdAndProjectIdAndSprintIdAndDeletedAtIsNull(Long workspaceId, Long projectId, Long sprintId);

    Optional<ProductivitySnapshot> findTopByWorkspaceIdAndProjectIdOrderByCreatedAtDesc(Long workspaceId, Long projectId);

    Page<ProductivitySnapshot> findByWorkspaceIdAndProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long workspaceId, Long projectId, Pageable pageable);
}



