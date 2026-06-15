package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.SprintForecastResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.SprintVelocitySnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.SprintForecastService;
import com.vinncorp.erp.modules.projects.service.VelocityService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SprintForecastServiceImpl implements SprintForecastService {

    private static final long CACHE_TTL = 300;

    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final SprintVelocitySnapshotRepository velocityRepository;
    private final VelocityService velocityService;
    private final CacheService cacheService;

    @Override
    public SprintForecastResponse forecast(Long sprintId) {
        String cacheKey = CacheNames.sprintForecast(sprintId);
        Optional<SprintForecastResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);
        int completed = 0;
        int total = taskSprints.size();

        for (TaskSprint ts : taskSprints) {
            Task task = ts.getTask();
            if (task != null && task.getStatusEntity() != null) {
                String status = task.getStatusEntity().getName().toUpperCase().trim();
                if ("DONE".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status)) {
                    completed++;
                }
            }
        }

        int remaining = total - completed;
        double avgVelocity = getAverageVelocity(sprint.getProject().getId());
        long totalDays = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate()) + 1;
        long daysElapsed = ChronoUnit.DAYS.between(sprint.getStartDate(), LocalDate.now());
        long daysRemaining = Math.max(0, totalDays - daysElapsed);

        double currentPace = daysElapsed > 0 ? (double) completed / daysElapsed : 0;
        double velocityPace = avgVelocity > 0 ? avgVelocity / totalDays : currentPace;
        double projectedCompletion = currentPace > 0
                ? completed + (currentPace * daysRemaining)
                : 0;
        double velocityAdjustedCompletion = velocityPace > 0
                ? completed + (velocityPace * daysRemaining)
                : projectedCompletion;

        double projectedCompletionRate = total > 0
                ? Math.min(100, (velocityAdjustedCompletion / total) * 100)
                : 0;

        int projectedSpillover = Math.max(0, total - (int) velocityAdjustedCompletion);
        LocalDate projectedCompletionDate = currentPace > 0
                ? LocalDate.now().plusDays((long) Math.ceil(remaining / Math.max(currentPace, 0.1)))
                : sprint.getEndDate();

        boolean onTrack = projectedCompletionRate >= 90 && projectedSpillover <= 1;

        SprintForecastResponse response = SprintForecastResponse.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getName())
                .completedPoints(completed)
                .remainingPoints(remaining)
                .averageVelocity(avgVelocity)
                .projectedCompletionRate(projectedCompletionRate)
                .projectedCompletionDate(projectedCompletionDate)
                .projectedSpillover(projectedSpillover)
                .onTrack(onTrack)
                .daysRemaining(daysRemaining)
                .daysElapsed(daysElapsed)
                .velocityAdjustedCompletion(velocityAdjustedCompletion)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    private double getAverageVelocity(Long projectId) {
        var trend = velocityService.getProjectVelocityHistory(projectId);
        return trend.getAverageVelocity() != null ? trend.getAverageVelocity() : 0;
    }

    @Override
    public void evictCache(Long sprintId) {
        cacheService.evict(CacheNames.sprintForecast(sprintId));
    }
}



