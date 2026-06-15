package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.SprintVelocitySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SprintVelocitySnapshotRepository extends JpaRepository<SprintVelocitySnapshot, Long> {

    Optional<SprintVelocitySnapshot> findBySprintId(Long sprintId);

    List<SprintVelocitySnapshot> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<SprintVelocitySnapshot> findTop5ByProjectIdOrderByCreatedAtDesc(Long projectId);

    boolean existsBySprintId(Long sprintId);

    List<SprintVelocitySnapshot> findByWorkspaceId(Long workspaceId);
}



