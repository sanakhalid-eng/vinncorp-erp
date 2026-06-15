package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.MonteCarloForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonteCarloForecastRepository extends JpaRepository<MonteCarloForecast, Long> {

    Optional<MonteCarloForecast> findTopByWorkspaceIdAndSprintIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long workspaceId, Long sprintId);
}



