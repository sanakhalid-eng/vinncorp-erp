package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.DeliveryPredictabilitySnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryPredictabilitySnapshotRepository extends JpaRepository<DeliveryPredictabilitySnapshot, Long> {

    Optional<DeliveryPredictabilitySnapshot> findTopByWorkspaceIdAndProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long workspaceId, Long projectId);

    Page<DeliveryPredictabilitySnapshot> findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(
            Long workspaceId, Long projectId, Pageable pageable);
}



