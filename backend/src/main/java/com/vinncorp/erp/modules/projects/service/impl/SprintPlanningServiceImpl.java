package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.request.SprintPlanRequest;
import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.engine.SprintOptimizationEngine;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.SprintPlanningService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintPlanningServiceImpl implements SprintPlanningService {

    private static final long CACHE_TTL = 300;
    private static final double DEFAULT_POINTS_PER_HOUR = 0.25;
    private static final int DEFAULT_VELOCITY = 20;

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final SprintCapacityRepository sprintCapacityRepository;
    private final SprintVelocitySnapshotRepository velocitySnapshotRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectRepository projectRepository;
    private final CacheService cacheService;
    private final SprintOptimizationEngine optimizationEngine;

    @Override
    @Transactional
    public SprintPlanResponse planSprint(Long workspaceId, Long projectId, SprintPlanRequest request) {
        Sprint sprint = sprintRepository.findById(request.getSprintId())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        List<Task> tasks;
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            tasks = taskRepository.findAllById(request.getTaskIds());
            Set<Long> foundIds = tasks.stream().map(Task::getId).collect(Collectors.toSet());
            for (Long reqId : request.getTaskIds()) {
                if (!foundIds.contains(reqId)) {
                    log.warn("Task {} not found, will be skipped", reqId);
                }
            }
        } else {
            tasks = taskRepository.findBacklogTasks(projectId);
        }

        if (!tasks.isEmpty()) {
            Task firstTask = tasks.get(0);
            if (firstTask.getProject() != null && !firstTask.getProject().getId().equals(projectId)) {
                tasks = tasks.stream()
                        .filter(t -> t.getProject() != null && t.getProject().getId().equals(projectId))
                        .collect(Collectors.toList());
            }
        }

        int availableCapacity = calculateAvailableCapacity(sprint.getId());
        if (request.getCapacityOverride() != null && request.getCapacityOverride() > 0) {
            availableCapacity = request.getCapacityOverride();
        }

        List<Task> selectedTasks = optimizationEngine.optimizeSprintComposition(tasks, availableCapacity);
        int totalPoints = selectedTasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        Map.Entry<Boolean, String> capacityCheck = optimizationEngine.detectOverCapacity(totalPoints, availableCapacity);
        List<String> warnings = new ArrayList<>();
        if (capacityCheck.getKey()) {
            warnings.add(capacityCheck.getValue());
        }

        List<TaskDependency> allDeps = taskDependencyRepository.findByAnyTaskIdIn(
                selectedTasks.stream().map(Task::getId).collect(Collectors.toList()));
        Set<Long> depConstrainedIds = allDeps.stream()
                .filter(d -> d.getDependsOnTask() != null
                        && selectedTasks.stream().noneMatch(t -> t.getId().equals(d.getDependsOnTask().getId())))
                .map(d -> d.getTask().getId())
                .collect(Collectors.toSet());

        List<SprintTaskRecommendation> taskRecs = selectedTasks.stream()
                .map(t -> SprintTaskRecommendation.builder()
                        .taskId(t.getId())
                        .taskTitle(t.getTitle())
                        .storyPoints(t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                        .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                        .assigneeName(t.getAssignee() != null ? t.getAssignee().getName() : null)
                        .priority(t.getPriority() != null ? t.getPriority().name() : "MEDIUM")
                        .isDependencyConstrained(depConstrainedIds.contains(t.getId()))
                        .build())
                .collect(Collectors.toList());

        double utilization = availableCapacity > 0 ? (double) totalPoints / availableCapacity : 0;

        SprintRecommendation recommendation = SprintRecommendation.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .committedPoints(totalPoints)
                .availableCapacity(availableCapacity)
                .utilizationPercent(utilization * 100)
                .tasks(taskRecs)
                .risks(warnings)
                .build();

        SprintPlanResponse response = SprintPlanResponse.builder()
                .recommendations(Collections.singletonList(recommendation))
                .totalTasks(selectedTasks.size())
                .totalPoints(totalPoints)
                .availableCapacity(availableCapacity)
                .isOverCapacity(capacityCheck.getKey())
                .warnings(warnings)
                .build();

        cacheService.evict(CacheNames.sprintPlanning(workspaceId));
        return response;
    }

    @Override
    public List<SprintRecommendation> getSprintRecommendations(Long workspaceId, Long projectId) {
        String cacheKey = CacheNames.sprintPlanning(workspaceId);
        Optional<List<SprintRecommendation>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Sprint> plannedSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.PLANNED.name());
        if (plannedSprints.isEmpty()) {
            cacheService.put(cacheKey, Collections.emptyList(), CACHE_TTL);
            return Collections.emptyList();
        }

        List<Task> backlogTasks = taskRepository.findBacklogTasks(projectId);

        double historicalVelocity = getHistoricalVelocity(projectId);
        int velocity = Math.max((int) historicalVelocity, DEFAULT_VELOCITY);

        List<SprintRecommendation> recommendations = new ArrayList<>();
        for (Sprint sprint : plannedSprints) {
            List<SprintCapacity> capacities = sprintCapacityRepository.findBySprintId(sprint.getId());
            int availableCapacity = calculateCapacityFromSprintCapacities(capacities);

            int capacityToUse = Math.max(availableCapacity, velocity);

            List<Task> suggestedTasks = optimizationEngine.optimizeSprintComposition(backlogTasks, capacityToUse);
            int totalPoints = suggestedTasks.stream()
                    .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                    .sum();

            List<TaskDependency> allDeps = taskDependencyRepository.findByAnyTaskIdIn(
                    suggestedTasks.stream().map(Task::getId).collect(Collectors.toList()));
            Set<Long> depConstrainedIds = allDeps.stream()
                    .filter(d -> d.getDependsOnTask() != null
                            && suggestedTasks.stream().noneMatch(t -> t.getId().equals(d.getDependsOnTask().getId())))
                    .map(d -> d.getTask().getId())
                    .collect(Collectors.toSet());

            List<SprintTaskRecommendation> taskRecs = suggestedTasks.stream()
                    .map(t -> SprintTaskRecommendation.builder()
                            .taskId(t.getId())
                            .taskTitle(t.getTitle())
                            .storyPoints(t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                            .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                            .assigneeName(t.getAssignee() != null ? t.getAssignee().getName() : null)
                            .priority(t.getPriority() != null ? t.getPriority().name() : "MEDIUM")
                            .isDependencyConstrained(depConstrainedIds.contains(t.getId()))
                            .build())
                    .collect(Collectors.toList());

            double utilization = capacityToUse > 0 ? (double) totalPoints / capacityToUse : 0;
            List<String> risks = new ArrayList<>();
            if (utilization > 1.0) risks.add("Over capacity by " + String.format("%.0f", (utilization - 1) * 100) + "%");
            if (utilization > 0.9) risks.add("High capacity utilization at " + String.format("%.0f", utilization * 100) + "%");

            recommendations.add(SprintRecommendation.builder()
                    .sprintId(sprint.getId())
                    .sprintName(sprint.getName())
                    .committedPoints(totalPoints)
                    .availableCapacity(capacityToUse)
                    .utilizationPercent(utilization * 100)
                    .tasks(taskRecs)
                    .risks(risks)
                    .build());
        }

        cacheService.put(cacheKey, recommendations, CACHE_TTL);
        return recommendations;
    }

    @Override
    public CapacityRiskResponse getCapacityRisks(Long workspaceId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        List<SprintCapacity> capacities = sprintCapacityRepository.findBySprintId(sprintId);
        int totalCapacity = calculateCapacityFromSprintCapacities(capacities);

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);
        int allocatedPoints = 0;
        for (TaskSprint ts : taskSprints) {
            if (ts.getTask() != null) {
                allocatedPoints += ts.getTask().getStoryPoints() != null ? ts.getTask().getStoryPoints() : 0;
            }
        }

        double allocationPercent = totalCapacity > 0 ? (double) allocatedPoints / totalCapacity * 100 : 0;
        boolean overloaded = allocatedPoints > totalCapacity;

        List<String> risks = new ArrayList<>();
        if (overloaded) {
            risks.add("Sprint is overloaded by " + (allocatedPoints - totalCapacity) + " points");
        }
        if (allocationPercent > 90 && !overloaded) {
            risks.add("Sprint allocation is at " + String.format("%.1f", allocationPercent) + "% - near capacity limit");
        }
        if (capacities.isEmpty()) {
            risks.add("No capacity defined for this sprint");
        }
        long membersOverCapacity = capacities.stream()
                .filter(c -> c.getAllocatedHours() > c.getAvailableHours())
                .count();
        if (membersOverCapacity > 0) {
            risks.add(membersOverCapacity + " team member(s) are over-allocated");
        }

        return CapacityRiskResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .totalCapacity(totalCapacity)
                .allocatedPoints(allocatedPoints)
                .allocationPercent(allocationPercent)
                .risks(risks)
                .isOverloaded(overloaded)
                .build();
    }

    @Override
    public SpilloverPredictionResponse getSpilloverPrediction(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Sprint> activeSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.ACTIVE.name());
        Sprint targetSprint = activeSprints.isEmpty() ? null : activeSprints.get(0);

        if (targetSprint == null) {
            List<Sprint> plannedSprints = sprintRepository
                    .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.PLANNED.name());
            targetSprint = plannedSprints.isEmpty() ? null : plannedSprints.get(0);
        }

        if (targetSprint == null) {
            return SpilloverPredictionResponse.builder()
                    .predictedSpilloverPoints(0)
                    .spilloverProbability(0)
                    .atRiskTasks(Collections.emptyList())
                    .build();
        }

        List<SprintVelocitySnapshot> history = velocitySnapshotRepository
                .findTop5ByProjectIdOrderByCreatedAtDesc(projectId);

        double avgSpilloverRate = 0;
        if (!history.isEmpty()) {
            avgSpilloverRate = history.stream()
                    .mapToDouble(h -> h.getCommittedPoints() > 0
                            ? (double) h.getSpilloverPoints() / h.getCommittedPoints()
                            : 0)
                    .average().orElse(0);
        }

        List<TaskSprint> currentTasks = taskSprintRepository.findBySprintIdWithTasks(targetSprint.getId());
        int totalCommitted = currentTasks.stream()
                .filter(ts -> ts.getTask() != null)
                .mapToInt(ts -> ts.getTask().getStoryPoints() != null ? ts.getTask().getStoryPoints() : 0)
                .sum();

        int predictedSpillover = (int) Math.round(totalCommitted * avgSpilloverRate);
        double probability = Math.min(avgSpilloverRate * 100, 100);

        List<String> atRiskTasks = new ArrayList<>();
        for (TaskSprint ts : currentTasks) {
            if (ts.getTask() != null) {
                Task task = ts.getTask();
                if (task.getDueDate() != null && task.getDueDate().isBefore(java.time.LocalDateTime.now().plusDays(3))) {
                    atRiskTasks.add(task.getTitle());
                }
            }
        }

        if (predictedSpillover == 0 && totalCommitted > 0) {
            predictedSpillover = (int) Math.round(totalCommitted * 0.15);
            probability = 15.0;
        }

        return SpilloverPredictionResponse.builder()
                .sprintId(targetSprint.getId())
                .sprintName(targetSprint.getName())
                .predictedSpilloverPoints(predictedSpillover)
                .spilloverProbability(Math.min(probability, 100))
                .atRiskTasks(atRiskTasks)
                .build();
    }

    private int calculateAvailableCapacity(Long sprintId) {
        List<SprintCapacity> capacities = sprintCapacityRepository.findBySprintId(sprintId);
        return calculateCapacityFromSprintCapacities(capacities);
    }

    private int calculateCapacityFromSprintCapacities(List<SprintCapacity> capacities) {
        if (capacities.isEmpty()) return DEFAULT_VELOCITY;
        double totalHours = capacities.stream()
                .mapToDouble(SprintCapacity::getAvailableHours)
                .sum();
        return (int) Math.round(totalHours * DEFAULT_POINTS_PER_HOUR);
    }

    private double getHistoricalVelocity(Long projectId) {
        List<SprintVelocitySnapshot> snapshots = velocitySnapshotRepository
                .findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.isEmpty()) return DEFAULT_VELOCITY;
        return snapshots.stream()
                .mapToDouble(SprintVelocitySnapshot::getVelocityScore)
                .average().orElse(DEFAULT_VELOCITY);
    }
}



