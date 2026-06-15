package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;

import com.vinncorp.erp.modules.projects.dto.response.MonteCarloForecastResponse;
import com.vinncorp.erp.modules.projects.engine.MonteCarloSimulator;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.repository.MonteCarloForecastRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.SprintVelocitySnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.MonteCarloForecastService;
import com.vinncorp.erp.modules.projects.service.VelocityPredictionService;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonteCarloForecastServiceImpl implements MonteCarloForecastService {

    private static final long CACHE_TTL_MS = 300_000L;
    private static final String CACHE_PREFIX = "sprint:montecarlo:";

    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final SprintVelocitySnapshotRepository velocitySnapshotRepository;
    private final MonteCarloForecastRepository forecastRepository;
    private final MonteCarloSimulator monteCarloSimulator;
    private final VelocityPredictionService velocityPredictionService;
    private final WorkspaceRepository workspaceRepository;
    private final CacheService cacheService;

    @Override
    @Transactional
    public MonteCarloForecastResponse forecast(Long workspaceId, Long sprintId) {
        String cacheKey = CACHE_PREFIX + sprintId;
        Optional<MonteCarloForecastResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Sprint sprint = requireSprintInWorkspace(workspaceId, sprintId);
        Long projectId = sprint.getProject().getId();

        double remaining = computeRemainingPoints(sprintId);
        double meanVelocity = velocityPredictionService.predictVelocity(projectId);
        double stdDev = computeVelocityStdDev(projectId);

        long daysRemaining = Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), sprint.getEndDate()) + 1);
        var result = monteCarloSimulator.simulate(
                remaining, meanVelocity, stdDev, LocalDate.now(), (int) daysRemaining);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        MonteCarloForecast entity = new MonteCarloForecast();
        entity.setWorkspace(workspace);
        entity.setProjectId(projectId);
        entity.setSprintId(sprintId);
        entity.setIterations(1000);
        entity.setP50CompletionDate(result.p50());
        entity.setP85CompletionDate(result.p85());
        entity.setP95CompletionDate(result.p95());
        entity.setMeanRemainingPoints(result.meanRemainingPoints());
        entity.setConfidenceScore(result.confidenceScore());
        forecastRepository.save(entity);

        MonteCarloForecastResponse response = MonteCarloForecastResponse.builder()
                .sprintId(sprintId)
                .projectId(projectId)
                .iterations(1000)
                .p50CompletionDate(result.p50())
                .p85CompletionDate(result.p85())
                .p95CompletionDate(result.p95())
                .meanRemainingPoints(result.meanRemainingPoints())
                .confidenceScore(result.confidenceScore())
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL_MS);
        return response;
    }

    private Sprint requireSprintInWorkspace(Long workspaceId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        if (!workspaceId.equals(sprint.getProject().getWorkspace().getId())) {
            throw new ResourceNotFoundException("Sprint not found");
        }
        return sprint;
    }

    private double computeRemainingPoints(Long sprintId) {
        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);
        int remaining = 0;
        for (TaskSprint ts : taskSprints) {
            Task task = ts.getTask();
            if (task == null || task.getStatusEntity() == null) {
                remaining++;
                continue;
            }
            String status = task.getStatusEntity().getName().toUpperCase().trim();
            if (!"DONE".equals(status) && !"COMPLETED".equals(status) && !"CLOSED".equals(status)) {
                remaining++;
            }
        }
        return remaining;
    }

    private double computeVelocityStdDev(Long projectId) {
        var snapshots = velocitySnapshotRepository.findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.isEmpty()) return 2.0;
        double mean = snapshots.stream().mapToInt(SprintVelocitySnapshot::getCompletedPoints).average().orElse(10);
        double variance = snapshots.stream()
                .mapToDouble(s -> Math.pow(s.getCompletedPoints() - mean, 2))
                .average().orElse(4);
        return Math.max(0.5, Math.sqrt(variance));
    }
}



