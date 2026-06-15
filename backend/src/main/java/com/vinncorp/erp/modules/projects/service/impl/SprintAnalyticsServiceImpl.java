package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.BurnDataPoint;
import com.vinncorp.erp.modules.projects.dto.response.BurndownDataPoint;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.SprintMetricSnapshot;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.repository.SprintMetricSnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.SprintAnalyticsService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintAnalyticsServiceImpl implements SprintAnalyticsService {

    private static final long CACHE_TTL = 300;

    private final SprintMetricSnapshotRepository metricRepository;
    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final CacheService cacheService;

    @Override
    public List<BurndownDataPoint> getBurndown(Long sprintId) {
        String cacheKey = CacheNames.sprintBurndown(sprintId);
        Optional<List<BurndownDataPoint>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        List<SprintMetricSnapshot> snapshots = metricRepository.findBySprintIdOrderBySnapshotDateAsc(sprintId);

        if (snapshots.isEmpty()) {
            List<BurndownDataPoint> empty = buildEmptyBurndown(sprint);
            cacheService.put(cacheKey, empty, CACHE_TTL);
            return empty;
        }

        double totalDays = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate()) + 1;
        int totalTasks = snapshots.getLast().getRemainingTasks()
                + snapshots.getLast().getCompletedTasks();

        List<BurndownDataPoint> data = new ArrayList<>();
        Map<LocalDate, SprintMetricSnapshot> snapshotMap = snapshots.stream()
                .collect(Collectors.toMap(SprintMetricSnapshot::getSnapshotDate, s -> s));

        for (LocalDate date = sprint.getStartDate(); !date.isAfter(sprint.getEndDate()); date = date.plusDays(1)) {
            double dayNumber = ChronoUnit.DAYS.between(sprint.getStartDate(), date);
            double idealRemaining = totalTasks - (totalTasks * dayNumber / Math.max(totalDays, 1));

            SprintMetricSnapshot snap = snapshotMap.get(date);
            int remaining = snap != null ? snap.getRemainingTasks() : 0;
            int completed = snap != null ? snap.getCompletedTasks() : 0;

            data.add(BurndownDataPoint.builder()
                    .date(date)
                    .remainingTasks(remaining)
                    .completedTasks(completed)
                    .totalTasks(totalTasks)
                    .idealRemaining(Math.max(0, idealRemaining))
                    .build());
        }

        cacheService.put(cacheKey, data, CACHE_TTL);
        return data;
    }

    @Override
    public List<BurnDataPoint> getBurnup(Long sprintId) {
        String cacheKey = CacheNames.sprintBurnup(sprintId);
        Optional<List<BurnDataPoint>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        List<SprintMetricSnapshot> snapshots = metricRepository.findBySprintIdOrderBySnapshotDateAsc(sprintId);

        if (snapshots.isEmpty()) {
            cacheService.put(cacheKey, Collections.emptyList(), CACHE_TTL);
            return Collections.emptyList();
        }

        double totalDays = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate()) + 1;
        int totalTasks = snapshots.getLast().getRemainingTasks()
                + snapshots.getLast().getCompletedTasks();

        List<BurnDataPoint> data = new ArrayList<>();
        Map<LocalDate, SprintMetricSnapshot> snapshotMap = snapshots.stream()
                .collect(Collectors.toMap(SprintMetricSnapshot::getSnapshotDate, s -> s));

        for (LocalDate date = sprint.getStartDate(); !date.isAfter(sprint.getEndDate()); date = date.plusDays(1)) {
            double dayNumber = ChronoUnit.DAYS.between(sprint.getStartDate(), date);
            double idealCompleted = totalTasks * dayNumber / Math.max(totalDays, 1);

            SprintMetricSnapshot snap = snapshotMap.get(date);
            int completed = snap != null ? snap.getCompletedTasks() : 0;
            int remaining = snap != null ? snap.getRemainingTasks() : 0;

            data.add(BurnDataPoint.builder()
                    .date(date)
                    .completedTasks(completed)
                    .totalTasks(totalTasks)
                    .remainingTasks(remaining)
                    .idealCompleted(Math.min(totalTasks, idealCompleted))
                    .build());
        }

        cacheService.put(cacheKey, data, CACHE_TTL);
        return data;
    }

    @Override
    @Transactional
    public void captureDailySnapshot(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        LocalDate today = LocalDate.now();

        if (metricRepository.existsBySprintIdAndSnapshotDate(sprintId, today)) return;
        if (sprint.getStatus() != SprintStatus.ACTIVE) return;

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);
        int remaining = 0;
        int completed = 0;

        for (TaskSprint ts : taskSprints) {
            Task task = ts.getTask();
            if (task != null && task.getStatusEntity() != null) {
                String status = task.getStatusEntity().getName().toUpperCase().trim();
                if ("DONE".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status)) {
                    completed++;
                } else {
                    remaining++;
                }
            } else {
                remaining++;
            }
        }

        SprintMetricSnapshot snapshot = new SprintMetricSnapshot();
        snapshot.setSprintId(sprintId);
        snapshot.setWorkspaceId(sprint.getProject().getWorkspace().getId());
        snapshot.setSnapshotDate(today);
        snapshot.setRemainingTasks(remaining);
        snapshot.setRemainingPoints(remaining);
        snapshot.setCompletedTasks(completed);
        snapshot.setCompletedPoints(completed);

        metricRepository.save(snapshot);
        evictCache(sprintId);
    }

    @Override
    public void captureDailySnapshotsForAllActive() {
        List<Sprint> activeSprints = sprintRepository.findByStatus(SprintStatus.ACTIVE.name());
        for (Sprint sprint : activeSprints) {
            try {
                captureDailySnapshot(sprint.getId());
            } catch (Exception e) {
                log.error("Failed to capture snapshot for sprint {}: {}", sprint.getId(), e.getMessage());
            }
        }
    }

    private List<BurndownDataPoint> buildEmptyBurndown(Sprint sprint) {
        List<BurndownDataPoint> data = new ArrayList<>();
        long totalDays = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate()) + 1;
        for (LocalDate date = sprint.getStartDate(); !date.isAfter(sprint.getEndDate()); date = date.plusDays(1)) {
            double dayNumber = ChronoUnit.DAYS.between(sprint.getStartDate(), date);
            double ideal = (totalTasks(sprint)) * (1 - dayNumber / Math.max(totalDays, 1));
            data.add(BurndownDataPoint.builder()
                    .date(date).remainingTasks(0).completedTasks(0)
                    .totalTasks(0).idealRemaining(Math.max(0, ideal)).build());
        }
        return data;
    }

    private int totalTasks(Sprint sprint) {
        return taskSprintRepository.findBySprintId(sprint.getId()).size();
    }

    @Override
    public void evictCache(Long sprintId) {
        cacheService.evict(CacheNames.sprintBurndown(sprintId));
        cacheService.evict(CacheNames.sprintBurnup(sprintId));
    }
}



