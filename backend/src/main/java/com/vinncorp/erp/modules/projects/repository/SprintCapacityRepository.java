package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.SprintCapacity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SprintCapacityRepository extends JpaRepository<SprintCapacity, Long> {

    List<SprintCapacity> findBySprintId(Long sprintId);

    Optional<SprintCapacity> findBySprintIdAndUserId(Long sprintId, Long userId);

    boolean existsBySprintIdAndUserId(Long sprintId, Long userId);

    void deleteBySprintId(Long sprintId);
}



