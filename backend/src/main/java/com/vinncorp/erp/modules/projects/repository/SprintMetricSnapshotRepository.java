package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.SprintMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SprintMetricSnapshotRepository extends JpaRepository<SprintMetricSnapshot, Long> {

    List<SprintMetricSnapshot> findBySprintIdOrderBySnapshotDateAsc(Long sprintId);

    Optional<SprintMetricSnapshot> findBySprintIdAndSnapshotDate(Long sprintId, LocalDate snapshotDate);

    boolean existsBySprintIdAndSnapshotDate(Long sprintId, LocalDate snapshotDate);

    void deleteBySprintId(Long sprintId);
}



