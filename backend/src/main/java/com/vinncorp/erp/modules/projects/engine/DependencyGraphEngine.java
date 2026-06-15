package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DependencyGraphEngine {

    private static final int MAX_NODES = 100;

    public GraphResult buildGraph(List<TaskDependency> dependencies) {
        List<TaskDependency> directional = filterDirectional(dependencies);
        Map<Long, Set<Long>> adjList = new HashMap<>();
        Map<Long, Set<Long>> reverseAdjList = new HashMap<>();
        Set<Long> allTaskIds = new HashSet<>();

        for (TaskDependency dep : directional) {
            Long prerequisite = getPrerequisite(dep);
            Long dependent = getDependent(dep);
            if (prerequisite == null || dependent == null) continue;

            allTaskIds.add(prerequisite);
            allTaskIds.add(dependent);

            adjList.computeIfAbsent(prerequisite, k -> new LinkedHashSet<>()).add(dependent);
            reverseAdjList.computeIfAbsent(dependent, k -> new LinkedHashSet<>()).add(prerequisite);
        }

        if (allTaskIds.size() > MAX_NODES) {
            log.warn("Dependency graph exceeds {} nodes, truncating to first {} nodes", MAX_NODES, MAX_NODES);
            List<Long> truncated = allTaskIds.stream().limit(MAX_NODES).collect(Collectors.toList());
            Set<Long> keep = new HashSet<>(truncated);
            adjList.keySet().retainAll(keep);
            adjList.values().forEach(s -> s.retainAll(keep));
            reverseAdjList.keySet().retainAll(keep);
            reverseAdjList.values().forEach(s -> s.retainAll(keep));
            allTaskIds.retainAll(keep);
        }

        return new GraphResult(adjList, reverseAdjList, allTaskIds);
    }

    public void validateNoCycle(Map<Long, Set<Long>> adjList, Set<Long> allTaskIds) {
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();

        for (Long nodeId : allTaskIds) {
            if (!visited.contains(nodeId)) {
                    if (hasCycleDfs(nodeId, adjList, visited, recursionStack, new HashSet<>())) {
                    throw new IllegalStateException(
                            "Cycle detected in task dependency graph. Tasks: " + recursionStack);
                    }
            }
        }
    }

    private boolean hasCycleDfs(Long nodeId, Map<Long, Set<Long>> adjList,
                                 Set<Long> visited, Set<Long> recursionStack,
                                 Set<Long> pathNodes) {
        visited.add(nodeId);
        recursionStack.add(nodeId);
        pathNodes.add(nodeId);

        Set<Long> neighbors = adjList.getOrDefault(nodeId, Collections.emptySet());
        for (Long neighbor : neighbors) {
            if (pathNodes.contains(neighbor)) {
                recursionStack.add(neighbor);
                return true;
            }
            if (!visited.contains(neighbor)) {
                if (hasCycleDfs(neighbor, adjList, visited, recursionStack, pathNodes)) {
                    return true;
                }
            }
        }

        recursionStack.remove(nodeId);
        pathNodes.remove(nodeId);
        return false;
    }

    public List<Long> topologicalSort(Map<Long, Set<Long>> adjList, Set<Long> allTaskIds) {
        Map<Long, Integer> inDegree = new HashMap<>();
        for (Long nodeId : allTaskIds) {
            inDegree.put(nodeId, 0);
        }
        for (Long nodeId : allTaskIds) {
            Set<Long> neighbors = adjList.getOrDefault(nodeId, Collections.emptySet());
            for (Long neighbor : neighbors) {
                inDegree.merge(neighbor, 1, Integer::sum);
            }
        }

        Deque<Long> queue = new ArrayDeque<>();
        for (Long nodeId : allTaskIds) {
            if (inDegree.getOrDefault(nodeId, 0) == 0) {
                queue.addLast(nodeId);
            }
        }

        List<Long> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long current = queue.removeFirst();
            topoOrder.add(current);
            Set<Long> neighbors = adjList.getOrDefault(current, Collections.emptySet());
            for (Long neighbor : neighbors) {
                int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.addLast(neighbor);
                }
            }
        }

        if (topoOrder.size() != allTaskIds.size()) {
            Set<Long> unprocessed = new HashSet<>(allTaskIds);
            topoOrder.forEach(unprocessed::remove);
            throw new IllegalStateException(
                    "Graph contains a cycle involving tasks: " + unprocessed);
        }

        return topoOrder;
    }

    public Map<Long, Integer> computeLongestPaths(List<Long> topoOrder, Map<Long, Set<Long>> adjList) {
        Map<Long, Integer> dist = new HashMap<>();
        for (Long nodeId : topoOrder) {
            dist.put(nodeId, 0);
        }

        for (Long nodeId : topoOrder) {
            int currentDist = dist.getOrDefault(nodeId, 0);
            Set<Long> neighbors = adjList.getOrDefault(nodeId, Collections.emptySet());
            for (Long neighbor : neighbors) {
                int newDist = currentDist + 1;
                if (newDist > dist.getOrDefault(neighbor, 0)) {
                    dist.put(neighbor, newDist);
                }
            }
        }

        return dist;
    }

    public int getCriticalPathLength(Map<Long, Integer> longestDistances) {
        return longestDistances.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public Set<Long> findCriticalPathTasks(Map<Long, Integer> longestDistFromStart,
                                            Map<Long, Set<Long>> adjList,
                                            Set<Long> allTaskIds,
                                            int criticalPathLength) {
        if (criticalPathLength == 0) return Collections.emptySet();
        Map<Long, Integer> longestDistToEnd = computeLongestDistancesToEnd(adjList, allTaskIds, criticalPathLength);
        Set<Long> criticalTasks = new HashSet<>();
        for (Long nodeId : allTaskIds) {
            int total = longestDistFromStart.getOrDefault(nodeId, 0)
                    + longestDistToEnd.getOrDefault(nodeId, 0);
            if (total == criticalPathLength) {
                criticalTasks.add(nodeId);
            }
        }
        return criticalTasks;
    }

    public Map<Long, Integer> computeLongestDistancesToEnd(Map<Long, Set<Long>> adjList,
                                                             Set<Long> allTaskIds,
                                                             int criticalPathLength) {
        Map<Long, Set<Long>> reverseAdj = new HashMap<>();
        for (Long nodeId : allTaskIds) {
            Set<Long> neighbors = adjList.getOrDefault(nodeId, Collections.emptySet());
            for (Long neighbor : neighbors) {
                reverseAdj.computeIfAbsent(neighbor, k -> new LinkedHashSet<>()).add(nodeId);
            }
        }

        Map<Long, Integer> dist = new HashMap<>();
        for (Long nodeId : allTaskIds) {
            dist.put(nodeId, 0);
        }

        List<Long> reverseTopo = new ArrayList<>(allTaskIds);
        Collections.reverse(reverseTopo);

        for (Long nodeId : reverseTopo) {
            int currentDist = dist.getOrDefault(nodeId, 0);
            Set<Long> predecessors = reverseAdj.getOrDefault(nodeId, Collections.emptySet());
            for (Long pred : predecessors) {
                int newDist = currentDist + 1;
                if (newDist > dist.getOrDefault(pred, 0)) {
                    dist.put(pred, newDist);
                }
            }
        }

        return dist;
    }

    public double computeCriticalityScore(Long taskId,
                                           Map<Long, Integer> longestFromStart,
                                           Map<Long, Integer> longestToEnd,
                                           int criticalPathLength) {
        if (criticalPathLength == 0) return 0.0;
        int total = longestFromStart.getOrDefault(taskId, 0)
                + longestToEnd.getOrDefault(taskId, 0);
        return (double) total / criticalPathLength;
    }

    private List<TaskDependency> filterDirectional(List<TaskDependency> dependencies) {
        return dependencies.stream()
                .filter(d -> d.getDependencyType() != null && d.getDependencyType().isDirectional())
                .filter(d -> !d.isDeleted())
                .collect(Collectors.toList());
    }

    private Long getPrerequisite(TaskDependency dep) {
        if (dep.getDependencyType() == DependencyType.BLOCKS) {
            return dep.getTask() != null ? dep.getTask().getId() : null;
        }
        return dep.getDependsOnTask() != null ? dep.getDependsOnTask().getId() : null;
    }

    private Long getDependent(TaskDependency dep) {
        if (dep.getDependencyType() == DependencyType.BLOCKS) {
            return dep.getDependsOnTask() != null ? dep.getDependsOnTask().getId() : null;
        }
        return dep.getTask() != null ? dep.getTask().getId() : null;
    }

    public record GraphResult(
            Map<Long, Set<Long>> adjacencyList,
            Map<Long, Set<Long>> reverseAdjacencyList,
            Set<Long> allTaskIds
    ) {}
}



