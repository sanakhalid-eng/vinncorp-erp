package com.vinncorp.erp.platform.audit.repository;

import com.vinncorp.erp.platform.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    Page<AuditEvent> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId, Pageable pageable);

    Page<AuditEvent> findByWorkspaceIdAndActionOrderByCreatedAtDesc(Long workspaceId, String action, Pageable pageable);

    Page<AuditEvent> findByWorkspaceIdAndEntityTypeOrderByCreatedAtDesc(Long workspaceId, String entityType, Pageable pageable);

    Page<AuditEvent> findByWorkspaceIdAndActorIdOrderByCreatedAtDesc(Long workspaceId, Long actorId, Pageable pageable);

    Page<AuditEvent> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.workspaceId = :workspaceId AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    Page<AuditEvent> findByDateRange(@Param("workspaceId") Long workspaceId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.workspaceId = :workspaceId AND LOWER(a.entityName) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.createdAt DESC")
    Page<AuditEvent> searchByKeyword(@Param("workspaceId") Long workspaceId,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    List<AuditEvent> findTop50ByWorkspaceIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(Long workspaceId, String entityType, Long entityId);

    @Query("SELECT a FROM AuditEvent a WHERE " +
           "(:workspaceId IS NULL OR a.workspaceId = :workspaceId) AND " +
           "(:actorId IS NULL OR a.actorId = :actorId) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditEvent> findByFilters(@Param("workspaceId") Long workspaceId,
                                   @Param("actorId") Long actorId,
                                   @Param("entityType") String entityType,
                                   @Param("action") String action,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);
}
