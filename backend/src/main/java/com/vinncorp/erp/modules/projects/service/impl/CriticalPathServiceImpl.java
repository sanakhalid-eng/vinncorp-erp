package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.CriticalPathResponse;
import com.vinncorp.erp.modules.projects.dto.response.CriticalTaskResponse;
import com.vinncorp.erp.modules.projects.dto.response.DeliveryRiskResponse;
import com.vinncorp.erp.modules.projects.dto.response.DependencyImpactResponse;
import com.vinncorp.erp.modules.projects.engine.DependencyGraphEngine;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.CriticalPathService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CriticalPathServiceImpl implements CriticalPathService {

    private static final long CACHE_TTL_SECONDS = 300;
    private static final int MAX_TRAVERSAL = 100;
    private static final double HIGH_RISK_THRESHOLD = 0.7;
    private static final double MEDIUM_RISK_THRESHOLD = 0.4;

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DependencyGraphEngine graphEngine;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public CriticalPathResponse getCriticalPath(Long workspaceId, Long projectId) {
        String cacheKey = CacheNames.criticalPath(projectId);
        Optional<CriticalPathResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        validateProjectInWorkspace(workspaceId, projectId);

        List<TaskDependency> allDeps = taskDependencyRepository.findByProjectId(projectId);
        DependencyGraphEngine.GraphResult graph = graphEngine.buildGraph(allDeps);

        if (graph.allTaskIds().isEmpty()) {
            CriticalPathResponse empty = CriticalPathResponse.builder()
                    .criticalTasks(Collections.emptyList())
                    .longestChainLength(0)
                    .totalCriticalityScore(0.0)
                    .calculatedAt(LocalDateTime.now())
                    .build();
            cacheService.put(cacheKey, empty, CACHE_TTL_SECONDS);
            return empty;
        }

        try {
            graphEngine.validateNoCycle(graph.adjacencyList(), graph.allTaskIds());
        } catch (IllegalStateException e) {
            log.warn("Cycle detected in project {}: {}", projectId, e.getMessage());
            CriticalPathResponse fallback = CriticalPathResponse.builder()
                    .criticalTasks(Collections.emptyList())
                    .longestChainLength(-1)
                    .totalCriticalityScore(0.0)
                    .calculatedAt(LocalDateTime.now())
                    .build();
            cacheService.put(cacheKey, fallback, CACHE_TTL_SECONDS);
            return fallback;
        }

        List<Long> topoOrder = graphEngine.topologicalSort(graph.adjacencyList(), graph.allTaskIds());
        Map<Long, Integer> longestFromStart = graphEngine.computeLongestPaths(topoOrder, graph.adjacencyList());
        int criticalPathLength = graphEngine.getCriticalPathLength(longestFromStart);

        Map<Long, Integer> longestToEnd = graphEngine.computeLongestDistancesToEnd(
                graph.adjacencyList(), graph.allTaskIds(), criticalPathLength);

        Set<Long> criticalTaskIds = graphEngine.findCriticalPathTasks(
                longestFromStart, graph.adjacencyList(), graph.allTaskIds(), criticalPathLength);

        Map<Long, Task> taskMap = fetchTaskMap(graph.allTaskIds());

        List<CriticalTaskResponse> criticalTasks = new ArrayList<>();
        double totalScore = 0.0;

        for (Long taskId : topoOrder) {
            int depth = longestFromStart.getOrDefault(taskId, 0);
            double score = graphEngine.computeCriticalityScore(taskId, longestFromStart, longestToEnd, criticalPathLength);
            boolean onCriticalPath = criticalTaskIds.contains(taskId);
            Task task = taskMap.get(taskId);

            criticalTasks.add(CriticalTaskResponse.builder()
                    .taskId(taskId)
                    .taskTitle(task != null ? task.getTitle() : "Unknown")
                    .dependencyDepth(depth)
                    .criticalityScore(score)
                    .isOnCriticalPath(onCriticalPath)
                    .calculatedAt(LocalDateTime.now())
                    .build());

            totalScore += score;
        }

        CriticalPathResponse response = CriticalPathResponse.builder()
                .criticalTasks(criticalTasks)
                .longestChainLength(criticalPathLength)
                .totalCriticalityScore(totalScore)
                .calculatedAt(LocalDateTime.now())
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL_SECONDS);
        publishCriticalPathEvent(projectId, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CriticalTaskResponse getTaskCriticality(Long workspaceId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        Long projectId = task.getProject().getId();
        String cacheKey = CacheNames.criticalPath(projectId);
        Optional<CriticalPathResponse> cached = cacheService.get(cacheKey);
        CriticalPathResponse pathResponse;
        if (cached.isPresent()) {
            pathResponse = cached.get();
        } else {
            pathResponse = getCriticalPath(workspaceId, projectId);
        }

        return pathResponse.getCriticalTasks().stream()
                .filter(ct -> ct.getTaskId().equals(taskId))
                .findFirst()
                .orElseGet(() -> {
                    List<TaskDependency> deps = taskDependencyRepository.findByProjectId(projectId);
                    DependencyGraphEngine.GraphResult graph = graphEngine.buildGraph(deps);
                    if (graph.allTaskIds().isEmpty() || graph.allTaskIds().contains(taskId)) {
                        return CriticalTaskResponse.builder()
                                .taskId(taskId)
                                .taskTitle(task.getTitle())
                                .dependencyDepth(0)
                                .criticalityScore(0.0)
                                .isOnCriticalPath(false)
                                .calculatedAt(LocalDateTime.now())
                                .build();
                    }
                    try {
                        graphEngine.validateNoCycle(graph.adjacencyList(), graph.allTaskIds());
                    } catch (IllegalStateException e) {
                        return CriticalTaskResponse.builder()
                                .taskId(taskId)
                                .taskTitle(task.getTitle())
                                .dependencyDepth(0)
                                .criticalityScore(0.0)
                                .isOnCriticalPath(false)
                                .calculatedAt(LocalDateTime.now())
                                .build();
                    }
                    List<Long> topoOrder = graphEngine.topologicalSort(graph.adjacencyList(), graph.allTaskIds());
                    Map<Long, Integer> longestFromStart = graphEngine.computeLongestPaths(topoOrder, graph.adjacencyList());
                    int criticalPathLength = graphEngine.getCriticalPathLength(longestFromStart);
                    Map<Long, Integer> longestToEnd = graphEngine.computeLongestDistancesToEnd(
                            graph.adjacencyList(), graph.allTaskIds(), criticalPathLength);
                    Set<Long> criticalTaskIds = graphEngine.findCriticalPathTasks(
                            longestFromStart, graph.adjacencyList(), graph.allTaskIds(), criticalPathLength);
                    double score = graphEngine.computeCriticalityScore(taskId, longestFromStart, longestToEnd, criticalPathLength);

                    return CriticalTaskResponse.builder()
                            .taskId(taskId)
                            .taskTitle(task.getTitle())
                            .dependencyDepth(longestFromStart.getOrDefault(taskId, 0))
                            .criticalityScore(score)
                            .isOnCriticalPath(criticalTaskIds.contains(taskId))
                            .calculatedAt(LocalDateTime.now())
                            .build();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryRiskResponse> getDeliveryRisks(Long workspaceId, Long projectId) {
        String cacheKey = CacheNames.executionRisk(projectId);
        Optional<List<DeliveryRiskResponse>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        validateProjectInWorkspace(workspaceId, projectId);

        List<TaskDependency> allDeps = taskDependencyRepository.findByProjectId(projectId);
        DependencyGraphEngine.GraphResult graph = graphEngine.buildGraph(allDeps);

        List<DeliveryRiskResponse> risks = new ArrayList<>();
        if (graph.allTaskIds().isEmpty()) {
            cacheService.put(cacheKey, risks, CACHE_TTL_SECONDS);
            return risks;
        }

        try {
            graphEngine.validateNoCycle(graph.adjacencyList(), graph.allTaskIds());
        } catch (IllegalStateException e) {
            cacheService.put(cacheKey, risks, CACHE_TTL_SECONDS);
            return risks;
        }

        List<Long> topoOrder = graphEngine.topologicalSort(graph.adjacencyList(), graph.allTaskIds());
        Map<Long, Integer> longestFromStart = graphEngine.computeLongestPaths(topoOrder, graph.adjacencyList());
        int criticalPathLength = graphEngine.getCriticalPathLength(longestFromStart);
        Map<Long, Integer> longestToEnd = graphEngine.computeLongestDistancesToEnd(
                graph.adjacencyList(), graph.allTaskIds(), criticalPathLength);
        Map<Long, Task> taskMap = fetchTaskMap(graph.allTaskIds());

        for (Long taskId : graph.allTaskIds()) {
            Task task = taskMap.get(taskId);
            if (task == null) continue;

            double score = graphEngine.computeCriticalityScore(taskId, longestFromStart, longestToEnd, criticalPathLength);

            boolean isOverdue = task.getDueDate() != null && task.getDueDate().isBefore(java.time.LocalDateTime.now());
            boolean isIncomplete = task.getStatusEntity() != null
                    && !isDoneStatus(task.getStatusEntity().getName());
            boolean isHeavilyBlocked = hasBlockedByDependencies(taskId);

            List<String> blockingFactors = new ArrayList<>();
            double delayProbability = 0.0;

            if (score >= HIGH_RISK_THRESHOLD) {
                delayProbability = 0.8 + (Math.random() * 0.15);
                if (isOverdue) {
                    delayProbability = Math.min(1.0, delayProbability + 0.1);
                    blockingFactors.add("Task is overdue");
                }
                if (isHeavilyBlocked) {
                    delayProbability = Math.min(1.0, delayProbability + 0.1);
                    blockingFactors.add("Has unresolved blocking dependencies");
                }
            } else if (score >= MEDIUM_RISK_THRESHOLD) {
                delayProbability = 0.4 + (Math.random() * 0.3);
                if (isOverdue) {
                    delayProbability = Math.min(1.0, delayProbability + 0.1);
                    blockingFactors.add("Task is overdue");
                }
                if (isHeavilyBlocked) {
                    blockingFactors.add("Has unresolved blocking dependencies");
                }
            } else {
                delayProbability = Math.random() * 0.3;
            }

            if (isIncomplete && score > MEDIUM_RISK_THRESHOLD) {
                blockingFactors.add("On or near critical path");
            }

            int estimatedDelayDays = (int) Math.round(delayProbability * Math.max(1, criticalPathLength - longestFromStart.getOrDefault(taskId, 0)));

            String riskLevel;
            if (delayProbability >= 0.7) riskLevel = "HIGH";
            else if (delayProbability >= 0.4) riskLevel = "MEDIUM";
            else riskLevel = "LOW";

            risks.add(DeliveryRiskResponse.builder()
                    .taskId(taskId)
                    .taskTitle(task.getTitle())
                    .delayProbability(Math.round(delayProbability * 100.0) / 100.0)
                    .estimatedDelayDays(estimatedDelayDays)
                    .riskLevel(riskLevel)
                    .blockingFactors(blockingFactors)
                    .build());
        }

        cacheService.put(cacheKey, risks, CACHE_TTL_SECONDS);
        return risks;
    }

    @Override
    @Transactional(readOnly = true)
    public DependencyImpactResponse getDependencyImpact(Long workspaceId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        Long projectId = task.getProject().getId();

        List<TaskDependency> allDeps = taskDependencyRepository.findByProjectId(projectId);
        DependencyGraphEngine.GraphResult graph = graphEngine.buildGraph(allDeps);

        int totalDependencies = (int) allDeps.stream()
                .filter(d -> d.getTask() != null && d.getTask().getId().equals(taskId)
                        && d.getDependencyType() != null && d.getDependencyType().isDirectional()
                        && !d.isDeleted())
                .count();

        int directDependents = (int) allDeps.stream()
                .filter(d -> d.getDependsOnTask() != null && d.getDependsOnTask().getId().equals(taskId)
                        && d.getDependencyType() != null && d.getDependencyType().isDirectional()
                        && !d.isDeleted())
                .count();

        int transitiveDependents = countTransitiveDependents(taskId, graph);

        double delayImpactMultiplier = transitiveDependents > 0
                ? 1.0 + (double) transitiveDependents / Math.max(1, graph.allTaskIds().size())
                : 1.0;

        delayImpactMultiplier = Math.round(delayImpactMultiplier * 100.0) / 100.0;

        String riskLevel;
        if (delayImpactMultiplier >= 2.0) riskLevel = "HIGH";
        else if (delayImpactMultiplier >= 1.5) riskLevel = "MEDIUM";
        else riskLevel = "LOW";

        return DependencyImpactResponse.builder()
                .taskId(taskId)
                .taskTitle(task.getTitle())
                .totalDependencies(totalDependencies)
                .directDependents(directDependents)
                .transitiveDependents(transitiveDependents)
                .delayImpactMultiplier(delayImpactMultiplier)
                .riskLevel(riskLevel)
                .build();
    }

    public void evictCache(Long projectId) {
        cacheService.evict(CacheNames.criticalPath(projectId));
        cacheService.evict(CacheNames.executionRisk(projectId));
    }

    private void validateProjectInWorkspace(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the specified workspace");
        }
    }

    private Map<Long, Task> fetchTaskMap(Set<Long> taskIds) {
        if (taskIds.isEmpty()) return Collections.emptyMap();
        List<Task> tasks = taskRepository.findAllById(new ArrayList<>(taskIds));
        Map<Long, Task> map = new HashMap<>();
        for (Task t : tasks) {
            if (t != null) map.put(t.getId(), t);
        }
        return map;
    }

    private boolean isDoneStatus(String name) {
        if (name == null) return false;
        String n = name.toUpperCase().trim();
        return "DONE".equals(n) || "COMPLETED".equals(n) || "CLOSED".equals(n);
    }

    private boolean hasBlockedByDependencies(Long taskId) {
        List<TaskDependency> deps = taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(taskId);
        return deps.stream().anyMatch(d -> d.getDependencyType() == DependencyType.BLOCKED_BY
                && d.getDependsOnTask() != null
                && d.getDependsOnTask().getStatusEntity() != null
                && !isDoneStatus(d.getDependsOnTask().getStatusEntity().getName()));
    }

    private int countTransitiveDependents(Long taskId, DependencyGraphEngine.GraphResult graph) {
        Set<Long> visited = new HashSet<>();
        Set<Long> dependents = new HashSet<>();
        countDependentsDfs(taskId, graph.adjacencyList(), visited, dependents, 0);
        return dependents.size();
    }

    private void countDependentsDfs(Long nodeId, Map<Long, Set<Long>> reverseAdj,
                                     Set<Long> visited, Set<Long> result, int depth) {
        if (depth > MAX_TRAVERSAL) return;
        Set<Long> successors = reverseAdj.getOrDefault(nodeId, Collections.emptySet());
        for (Long successor : successors) {
            if (result.add(successor)) {
                countDependentsDfs(successor, reverseAdj, visited, result, depth + 1);
            }
        }
    }

    private void publishCriticalPathEvent(Long projectId, CriticalPathResponse response) {
        try {
            eventPublisher.publish(DomainEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(DomainEvent.Type.CRITICAL_PATH_CHANGED)
                    .entityType("PROJECT")
                    .entityId(projectId)
                    .projectId(projectId)
                    .message("Critical path recalculated: length=" + response.getLongestChainLength()
                            + ", tasks=" + response.getCriticalTasks().size())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to publish critical path event for project {}: {}", projectId, e.getMessage());
        }
    }
}



