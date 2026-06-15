package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    Optional<Opportunity> findByIdAndWorkspaceId(Long id, Long workspaceId);
    List<Opportunity> findAllByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);
    List<Opportunity> findByWorkspaceIdAndStageIdOrderByCreatedAtDesc(Long workspaceId, Long stageId);
    List<Opportunity> findByOwnerIdAndWorkspaceIdOrderByCreatedAtDesc(Long ownerId, Long workspaceId);
    List<Opportunity> findByCustomerIdAndWorkspaceIdOrderByCreatedAtDesc(Long customerId, Long workspaceId);

    @Query("SELECT COALESCE(SUM(o.value), 0) FROM Opportunity o WHERE o.workspace.id = :wsId AND o.stage.isWon = false AND o.stage.isLost = false")
    BigDecimal sumPipelineValue(@Param("wsId") Long workspaceId);

    @Query("SELECT COALESCE(SUM(o.value), 0) FROM Opportunity o WHERE o.workspace.id = :wsId AND o.stage.isWon = true")
    BigDecimal sumWonValue(@Param("wsId") Long workspaceId);

    long countByWorkspaceIdAndStageIsWonAndStageIsLost(Long workspaceId, boolean isWon, boolean isLost);

    @Query("SELECT o FROM Opportunity o WHERE o.workspace.id = :wsId AND o.stage.isWon = false AND o.stage.isLost = false ORDER BY o.createdAt DESC")
    List<Opportunity> findOpenOpportunities(@Param("wsId") Long workspaceId);

    List<Opportunity> findByLeadIdAndWorkspaceIdOrderByCreatedAtDesc(Long leadId, Long workspaceId);

    @Query("SELECT COALESCE(SUM(o.value), 0) FROM Opportunity o WHERE o.customerId = :customerId AND o.workspace.id = :wsId AND o.stage.isWon = true")
    BigDecimal sumWonValueByCustomerId(@Param("customerId") Long customerId, @Param("wsId") Long workspaceId);

    long countByCustomerIdAndWorkspaceIdAndStageIsWon(Long customerId, Long workspaceId, boolean isWon);

    long countByCustomerIdAndWorkspaceIdAndStageIsWonAndStageIsLost(Long customerId, Long workspaceId, boolean isWon, boolean isLost);

    @Query("SELECT COUNT(o) FROM Opportunity o WHERE o.customerId = :customerId AND o.workspace.id = :wsId AND o.stage.isWon = false AND o.stage.isLost = false")
    long countOpenByCustomerId(@Param("customerId") Long customerId, @Param("wsId") Long workspaceId);
}
