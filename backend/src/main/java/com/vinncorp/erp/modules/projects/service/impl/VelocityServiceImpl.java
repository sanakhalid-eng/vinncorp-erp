package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.VelocityResponse;
import com.vinncorp.erp.modules.projects.dto.response.VelocityTrendResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.SprintVelocitySnapshot;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.SprintVelocitySnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.VelocityService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VelocityServiceImpl implements VelocityService {

    private static final long CACHE_TTL = 300;

    private final SprintVelocitySnapshotRepository snapshotRepository;
    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final CacheService cacheService;

    @Override
    public VelocityResponse getSprintVelocity(Long sprintId) {
        String cacheKey = CacheNames.sprintVelocity(sprintId);
        Optional<VelocityResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        SprintVelocitySnapshot snapshot = snapshotRepository.findBySprintId(sprintId)
                .orElse(null);
        if (snapshot == null) {
            Sprint sprint = sprintRepository.findById(sprintId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
            snapshot = computeAndSave(sprint);
        }

        VelocityResponse response = toVelocityResponse(snapshot);
        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    @Override
    public VelocityTrendResponse getProjectVelocityHistory(Long projectId) {
        String cacheKey = CacheNames.projectVelocity(projectId);
        Optional<VelocityTrendResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        List<SprintVelocitySnapshot> snapshots = snapshotRepository
                .findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.isEmpty()) {
            return VelocityTrendResponse.builder()
                    .trend("STABLE")
                    .averageVelocity(0.0)
                    .history(Collections.emptyList())
                    .build();
        }

        double avgVelocity = snapshots.stream()
                .mapToDouble(SprintVelocitySnapshot::getVelocityScore)
                .average().orElse(0);

        double last5Avg = snapshots.stream()
                .limit(5)
                .mapToDouble(SprintVelocitySnapshot::getVelocityScore)
                .average().orElse(0);

        String trend = determineTrend(snapshots);
        Double changePct = snapshots.size() >= 2
                ? ((snapshots.getFirst().getVelocityScore() - snapshots.getLast().getVelocityScore())
                / Math.max(snapshots.getLast().getVelocityScore(), 0.01)) * 100
                : 0.0;

        List<VelocityResponse> history = snapshots.stream()
                .map(this::toVelocityResponse)
                .collect(Collectors.toList());

        VelocityResponse best = history.stream()
                .max(Comparator.comparingDouble(VelocityResponse::getVelocityScore))
                .orElse(null);
        VelocityResponse worst = history.stream()
                .min(Comparator.comparingDouble(VelocityResponse::getVelocityScore))
                .orElse(null);

        VelocityTrendResponse response = VelocityTrendResponse.builder()
                .trend(trend)
                .changePercentage(changePct)
                .averageVelocity(avgVelocity)
                .lastFiveAverage(last5Avg)
                .bestSprint(best)
                .worstSprint(worst)
                .history(history)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    @Override
    @Transactional
    public void generateVelocitySnapshot(Long sprintId) {
        if (snapshotRepository.existsBySprintId(sprintId)) return;
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        computeAndSave(sprint);
    }

    private SprintVelocitySnapshot computeAndSave(Sprint sprint) {
        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprint.getId());
        int total = taskSprints.size();
        int completed = 0;

        for (TaskSprint ts : taskSprints) {
            Task task = ts.getTask();
            if (task != null && task.getStatusEntity() != null) {
                String status = task.getStatusEntity().getName().toUpperCase().trim();
                if ("DONE".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status)) {
                    completed++;
                }
            }
        }

        int carriedForward = 0;
        if (sprint.getSummaryCarriedForward() != null) {
            carriedForward = sprint.getSummaryCarriedForward();
        }

        int committed = total + carriedForward;
        double completionRate = committed > 0 ? (double) completed / committed : 0;
        double velocityScore = completionRate * 100;

        SprintVelocitySnapshot snapshot = new SprintVelocitySnapshot();
        snapshot.setSprintId(sprint.getId());
        snapshot.setWorkspaceId(sprint.getProject().getWorkspace().getId());
        snapshot.setProjectId(sprint.getProject().getId());
        snapshot.setCommittedPoints(committed);
        snapshot.setCompletedPoints(completed);
        snapshot.setSpilloverPoints(committed - completed);
        snapshot.setCompletionRate(completionRate);
        snapshot.setVelocityScore(velocityScore);

        return snapshotRepository.save(snapshot);
    }

    private String determineTrend(List<SprintVelocitySnapshot> snapshots) {
        if (snapshots.size() < 3) return "STABLE";
        double recent = snapshots.stream().limit(2)
                .mapToDouble(SprintVelocitySnapshot::getVelocityScore).average().orElse(0);
        double older = snapshots.stream().skip(snapshots.size() - 2)
                .mapToDouble(SprintVelocitySnapshot::getVelocityScore).average().orElse(0);
        double diff = recent - older;
        if (diff > 5) return "INCREASING";
        if (diff < -5) return "DECREASING";
        return "STABLE";
    }

    private VelocityResponse toVelocityResponse(SprintVelocitySnapshot s) {
        String sprintName = sprintRepository.findById(s.getSprintId())
                .map(Sprint::getName).orElse("Unknown");
        return VelocityResponse.builder()
                .sprintId(s.getSprintId())
                .sprintName(sprintName)
                .committedPoints(s.getCommittedPoints())
                .completedPoints(s.getCompletedPoints())
                .spilloverPoints(s.getSpilloverPoints())
                .completionRate(s.getCompletionRate())
                .velocityScore(s.getVelocityScore())
                .build();
    }

    @Override
    public void evictCache(Long sprintId) {
        cacheService.evict(CacheNames.sprintVelocity(sprintId));
    }
}



