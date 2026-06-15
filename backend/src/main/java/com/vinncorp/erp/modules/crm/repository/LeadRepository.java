package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.Lead;
import com.vinncorp.erp.modules.crm.enums.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    Optional<Lead> findByIdAndWorkspaceId(Long id, Long workspaceId);
    List<Lead> findByWorkspaceIdAndStatusOrderByCreatedAtDesc(Long workspaceId, LeadStatus status);
    List<Lead> findAllByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);
    long countByWorkspaceIdAndStatus(Long workspaceId, LeadStatus status);
    long countByWorkspaceIdAndStatusIn(Long workspaceId, List<LeadStatus> statuses);

    @Query("SELECT l FROM Lead l WHERE l.workspace.id = :wsId AND (LOWER(l.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(l.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(l.company) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY l.createdAt DESC")
    List<Lead> search(@Param("wsId") Long workspaceId, @Param("q") String query);
}
