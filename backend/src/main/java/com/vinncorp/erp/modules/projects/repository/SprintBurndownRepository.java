package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.SprintBurndown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SprintBurndownRepository extends JpaRepository<SprintBurndown, Long> {

    List<SprintBurndown> findBySprintIdOrderByDateAsc(Long sprintId);

    Optional<SprintBurndown> findBySprintIdAndDate(Long sprintId, LocalDate date);

    @Modifying
    @Query("DELETE FROM SprintBurndown sb WHERE sb.sprint.id = :sprintId")
    void deleteBySprintId(@Param("sprintId") Long sprintId);

    @Query("SELECT sb FROM SprintBurndown sb WHERE sb.date BETWEEN :startDate AND :endDate ORDER BY sb.date ASC")
    List<SprintBurndown> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}



