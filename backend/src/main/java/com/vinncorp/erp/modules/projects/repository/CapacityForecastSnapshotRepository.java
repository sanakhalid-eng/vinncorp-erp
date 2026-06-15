package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.CapacityForecastSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CapacityForecastSnapshotRepository extends JpaRepository<CapacityForecastSnapshot, Long> {

    Page<CapacityForecastSnapshot> findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(
            Long workspaceId, Long projectId, Pageable pageable);

    Optional<CapacityForecastSnapshot> findTopByWorkspaceIdAndSprintIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long workspaceId, Long sprintId);
}



