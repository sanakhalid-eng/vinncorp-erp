package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ActivityLog;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    Page<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType entityType, Long entityId, Pageable pageable);

    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ActivityLog> findTop10ByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<ActivityLog> findTop10ByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType entityType, Long entityId);

    @Query("SELECT a FROM ActivityLog a WHERE " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:projectId IS NULL OR a.project.id = :projectId) AND " +
           "(:workspaceId IS NULL OR a.workspaceId = :workspaceId) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) AND " +
           "(:securityOnly = false OR a.action IN ('ROLE_EDIT_BLOCKED', 'ROLE_DELETE_BLOCKED', 'ADMIN_ASSIGNMENT_BLOCKED', 'OWNERSHIP_TRANSFERRED', 'WEBHOOK_BLOCKED', 'SECURITY_VALIDATION_FAILED')) " +
           "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByFilters(
            @Param("userId") Long userId,
            @Param("entityType") EntityType entityType,
            @Param("action") ActionType action,
            @Param("projectId") Long projectId,
            @Param("workspaceId") Long workspaceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("securityOnly") boolean securityOnly,
            Pageable pageable
    );

    Page<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId, Pageable pageable);

    long countByWorkspaceIdAndCreatedAtBetween(Long workspaceId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM ActivityLog a WHERE " +
           "a.metadata IS NOT NULL AND a.metadata LIKE %:keyword%")
    Page<ActivityLog> findByMetadataContaining(@Param("keyword") String keyword, Pageable pageable);

    List<ActivityLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}



