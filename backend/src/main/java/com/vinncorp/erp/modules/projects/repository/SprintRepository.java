package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectIdOrderByStartDateDesc(Long projectId);

    Optional<Sprint> findByProjectIdAndStatus(Long projectId, String status);

    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status = com.vinncorp.erp.modules.projects.enums.SprintStatus.ACTIVE")
    Optional<Sprint> findActiveSprintByProjectId(@Param("projectId") Long projectId);

    boolean existsByProjectIdAndStatus(Long projectId, String status);

    List<Sprint> findByProjectIdAndStatusOrderByStartDateDesc(Long projectId, String status);

    List<Sprint> findByStatus(String status);

    @Query("SELECT s FROM Sprint s WHERE s.startDate <= :date AND s.endDate >= :date AND s.status = com.vinncorp.erp.modules.projects.enums.SprintStatus.ACTIVE")
    List<Sprint> findActiveSprintsByDate(@Param("date") LocalDate date);
}



