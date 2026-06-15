package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.SprintHealthResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.SprintCapacity;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.enums.SprintHealthStatus;
import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import com.vinncorp.erp.modules.projects.repository.SprintCapacityRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.SprintHealthService;
import com.vinncorp.erp.modules.projects.service.TaskDependencyGraphService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SprintHealthServiceImpl implements SprintHealthService {

    private static final long CACHE_TTL = 300;

    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyGraphService dependencyGraphService;
    private final SprintCapacityRepository capacityRepository;
    private final CacheService cacheService;

    @Override
    public SprintHealthResponse assessHealth(Long sprintId) {
        String cacheKey = CacheNames.sprintHealth(sprintId);
        Optional<SprintHealthResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);
        List<String> issues = new ArrayList<>();
        int overdueTasks = 0;
        int blockedTasks = 0;
        int unresolvedCritical = 0;
        int completed = 0;
        int total = taskSprints.size();

        for (TaskSprint ts : taskSprints) {
            Task task = ts.getTask();
            if (task == null) continue;

            boolean isDone = task.getStatusEntity() != null && isDoneStatus(task.getStatusEntity().getName());
            if (isDone) { completed++; continue; }

            if (task.getDueDate() != null && task.getDueDate().toLocalDate().isBefore(LocalDate.now())) {
                overdueTasks++;
            }
            try {
                var blockedStatus = dependencyGraphService.getBlockedStatus(task.getId());
                if (blockedStatus != null && blockedStatus.isBlocked()) {
                    blockedTasks++;
                }
            } catch (Exception ignored) {}

            if (task.getPriority() == TaskPriority.CRITICAL || task.getPriority() == TaskPriority.HIGH) {
                unresolvedCritical++;
            }
        }

        double completionRate = total > 0 ? (double) completed / total : 0;
        double spilloverTrend = computeSpilloverTrend(sprint);
        int overloadedMembers = countOverloadedMembers(sprintId);

        double score = computeHealthScore(completionRate, overdueTasks, blockedTasks,
                overloadedMembers, unresolvedCritical, total);

        if (overdueTasks > 0) issues.add(overdueTasks + " overdue tasks");
        if (blockedTasks > 0) issues.add(blockedTasks + " blocked tasks");
        if (overloadedMembers > 0) issues.add(overloadedMembers + " overloaded members");
        if (spilloverTrend > 20) issues.add("spillover trend increasing");
        if (unresolvedCritical > 0) issues.add(unresolvedCritical + " unresolved critical tasks");
        if (completionRate < 0.3) issues.add("completion rate below 30%");

        SprintHealthStatus status = score >= 70 ? SprintHealthStatus.HEALTHY
                : score >= 40 ? SprintHealthStatus.AT_RISK
                : SprintHealthStatus.CRITICAL;

        SprintHealthResponse response = SprintHealthResponse.builder()
                .score(score)
                .status(status)
                .issues(issues)
                .overdueTasks(overdueTasks)
                .blockedTasks(blockedTasks)
                .spilloverTrend(spilloverTrend)
                .completionRate(completionRate * 100)
                .overloadedMembers(overloadedMembers)
                .unresolvedCriticalTasks(unresolvedCritical)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    private double computeHealthScore(double completionRate, int overdue, int blocked,
                                       int overloaded, int critical, int total) {
        double score = 100;
        score -= (1 - completionRate) * 30;
        score -= Math.min(overdue * 5, 20);
        score -= Math.min(blocked * 3, 15);
        score -= Math.min(overloaded * 5, 15);
        score -= Math.min(critical * 8, 20);
        return Math.max(0, Math.min(100, score));
    }

    private double computeSpilloverTrend(Sprint sprint) {
        if (sprint.getSummaryCarriedForward() == null || sprint.getSummaryTotalTasks() == null
                || sprint.getSummaryTotalTasks() == 0) return 0;
        return (double) sprint.getSummaryCarriedForward() / sprint.getSummaryTotalTasks() * 100;
    }

    private int countOverloadedMembers(Long sprintId) {
        List<SprintCapacity> capacities = capacityRepository.findBySprintId(sprintId);
        return (int) capacities.stream().filter(c -> c.getUtilizationPercent() > 100).count();
    }

    private boolean isDoneStatus(String name) {
        if (name == null) return false;
        String n = name.toUpperCase().trim();
        return "DONE".equals(n) || "COMPLETED".equals(n) || "CLOSED".equals(n);
    }

    @Override
    public void evictCache(Long sprintId) {
        cacheService.evict(CacheNames.sprintHealth(sprintId));
    }
}



