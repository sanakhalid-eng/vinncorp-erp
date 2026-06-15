package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.BlockedStatusResponse;
import com.vinncorp.erp.modules.projects.dto.response.DependencyGraphResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskDependencyResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import com.vinncorp.erp.modules.projects.mapper.TaskDependencyMapper;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.shared.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskDependencyGraphService {

    private static final long CACHE_TTL_SECONDS = 300;

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;
    private final CacheService cacheService;

    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> getDependencies(Long taskId) {
        return taskDependencyRepository.findByTaskIdWithDependsOn(taskId).stream()
                .map(TaskDependencyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> getBlockingTasks(Long taskId) {
        return taskDependencyRepository.findByDependsOnTaskIdWithTask(taskId).stream()
                .map(TaskDependencyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> getRelatedTasks(Long taskId) {
        List<TaskDependency> outgoing = taskDependencyRepository.findByTaskIdAndType(taskId, DependencyType.RELATES_TO);
        List<TaskDependency> incoming = taskDependencyRepository.findByDependsOnTaskIdAndType(taskId, DependencyType.RELATES_TO);
        List<TaskDependency> all = new ArrayList<>(outgoing);
        all.addAll(incoming);
        return all.stream()
                .map(TaskDependencyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BlockedStatusResponse getBlockedStatus(Long taskId) {
        String cacheKey = "dep:blocked:" + taskId;
        Optional<BlockedStatusResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        List<TaskDependency> blocking = taskDependencyRepository.findBlockingDependenciesForTask(taskId);

        List<BlockedStatusResponse.BlockingTaskInfo> blockingTasks = blocking.stream()
                .filter(dep -> {
                    if (dep.getDependsOnTask() == null || dep.getDependsOnTask().getStatusEntity() == null) return false;
                    String normalized = dep.getDependsOnTask().getStatusEntity().getName().toUpperCase().trim();
                    return !(normalized.equals("DONE") || normalized.equals("COMPLETED") || normalized.equals("CLOSED"));
                })
                .map(dep -> BlockedStatusResponse.BlockingTaskInfo.builder()
                        .taskId(dep.getDependsOnTask().getId())
                        .taskTitle(dep.getDependsOnTask().getTitle())
                        .status(dep.getDependsOnTask().getStatusEntity() != null ?
                                dep.getDependsOnTask().getStatusEntity().getName() : null)
                        .dependencyId(dep.getId())
                        .dependencyType(dep.getDependencyType().name())
                        .description(dep.getDescription())
                        .build())
                .collect(Collectors.toList());

        BlockedStatusResponse response = BlockedStatusResponse.builder()
                .blocked(!blockingTasks.isEmpty())
                .blockingTasks(blockingTasks)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL_SECONDS);
        return response;
    }

    @Transactional(readOnly = true)
    public DependencyGraphResponse buildDependencyGraph(Long taskId) {
        String cacheKey = "dep:graph:" + taskId;
        Optional<DependencyGraphResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Set<Long> visited = new HashSet<>();
        List<TaskDependency> allEdges = new ArrayList<>();
        collectGraph(taskId, visited, allEdges, 0);

        Set<Long> nodeIds = new HashSet<>();
        List<DependencyGraphResponse.GraphEdge> edges = new ArrayList<>();

        for (TaskDependency dep : allEdges) {
            edges.add(DependencyGraphResponse.GraphEdge.builder()
                    .id(dep.getId())
                    .sourceId(dep.getTask().getId())
                    .targetId(dep.getDependsOnTask().getId())
                    .dependencyType(dep.getDependencyType().name())
                    .description(dep.getDescription())
                    .build());
            nodeIds.add(dep.getTask().getId());
            nodeIds.add(dep.getDependsOnTask().getId());
        }

        List<Task> taskList = taskRepository.findAllById(new ArrayList<>(nodeIds));
        List<DependencyGraphResponse.GraphNode> nodes = taskList.stream()
                .map(t -> DependencyGraphResponse.GraphNode.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .status(t.getStatusEntity() != null ? t.getStatusEntity().getName() : null)
                        .priority(t.getPriority() != null ? t.getPriority().name() : null)
                        .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                        .assigneeName(t.getAssignee() != null ? t.getAssignee().getName() : null)
                        .build())
                .collect(Collectors.toList());

        DependencyGraphResponse response = DependencyGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL_SECONDS);
        return response;
    }

    private void collectGraph(Long taskId, Set<Long> visited, List<TaskDependency> edges, int depth) {
        if (depth > 10) return;
        if (visited.contains(taskId)) return;
        visited.add(taskId);

        List<TaskDependency> outgoing = taskDependencyRepository.findByTaskIdWithDependsOn(taskId);
        List<TaskDependency> incoming = taskDependencyRepository.findByDependsOnTaskIdWithTask(taskId);

        for (TaskDependency dep : outgoing) {
            edges.add(dep);
            if (dep.getDependsOnTask() != null) {
                collectGraph(dep.getDependsOnTask().getId(), visited, edges, depth + 1);
            }
        }
        for (TaskDependency dep : incoming) {
            edges.add(dep);
            if (dep.getTask() != null) {
                collectGraph(dep.getTask().getId(), visited, edges, depth + 1);
            }
        }
    }

    @Transactional(readOnly = true)
    public long getDependencyCount(Long taskId) {
        return taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(taskId).size();
    }

    @Transactional(readOnly = true)
    public long getBlockedTaskCount(Long projectId) {
        List<TaskDependency> deps = taskDependencyRepository.findByProjectId(projectId);
        Set<Long> uniqueBlockedIds = new HashSet<>();

        for (TaskDependency dep : deps) {
            if (dep.getDependencyType() == DependencyType.BLOCKED_BY && dep.getDependsOnTask() != null) {
                Task block = dep.getDependsOnTask();
                if (block.getStatusEntity() != null) {
                    String normalized = block.getStatusEntity().getName().toUpperCase().trim();
                    if (!(normalized.equals("DONE") || normalized.equals("COMPLETED") || normalized.equals("CLOSED"))) {
                        uniqueBlockedIds.add(dep.getTask().getId());
                    }
                }
            }
        }
        return uniqueBlockedIds.size();
    }

    public void evictCache(Long taskId) {
        cacheService.evict("dep:blocked:" + taskId);
        cacheService.evict("dep:graph:" + taskId);
    }
}



