package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.entity.SprintCapacity;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SprintOptimizationEngine {

    private static final double POINTS_PER_HOUR = 0.25;

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;

    public List<Task> optimizeSprintComposition(List<Task> availableTasks, int capacityPoints) {
        Map<TaskPriority, Integer> priorityOrder = new EnumMap<>(TaskPriority.class);
        priorityOrder.put(TaskPriority.CRITICAL, 0);
        priorityOrder.put(TaskPriority.HIGH, 1);
        priorityOrder.put(TaskPriority.MEDIUM, 2);
        priorityOrder.put(TaskPriority.LOW, 3);

        Set<Long> taskIds = availableTasks.stream().map(Task::getId).collect(Collectors.toSet());
        List<TaskDependency> allDeps = taskDependencyRepository.findByAnyTaskIdIn(new ArrayList<>(taskIds));
        Set<Long> depConstrainedIds = new HashSet<>();
        for (TaskDependency dep : allDeps) {
            if (dep.getDependsOnTask() != null && !taskIds.contains(dep.getDependsOnTask().getId())) {
                depConstrainedIds.add(dep.getTask().getId());
            }
        }

        List<Task> prioritized = availableTasks.stream()
                .sorted((a, b) -> {
                    int pa = priorityOrder.getOrDefault(a.getPriority() != null ? a.getPriority() : TaskPriority.LOW, 3);
                    int pb = priorityOrder.getOrDefault(b.getPriority() != null ? b.getPriority() : TaskPriority.LOW, 3);
                    if (pa != pb) return Integer.compare(pa, pb);
                    boolean aDep = depConstrainedIds.contains(a.getId());
                    boolean bDep = depConstrainedIds.contains(b.getId());
                    if (aDep != bDep) return Boolean.compare(aDep, bDep);
                    return Integer.compare(b.getStoryPoints() != null ? b.getStoryPoints() : 0,
                            a.getStoryPoints() != null ? a.getStoryPoints() : 0);
                })
                .toList();

        List<Task> selected = new ArrayList<>();
        int usedPoints = 0;
        for (Task task : prioritized) {
            int sp = task.getStoryPoints() != null ? task.getStoryPoints() : 0;
            if (sp == 0) sp = 1;
            if (usedPoints + sp <= capacityPoints) {
                selected.add(task);
                usedPoints += sp;
            }
        }

        return selected;
    }

    public Map.Entry<Boolean, String> detectOverCapacity(int allocatedPoints, int availableCapacity) {
        if (availableCapacity <= 0) {
            return new AbstractMap.SimpleEntry<>(true, "No capacity defined for this sprint");
        }
        double utilization = (double) allocatedPoints / availableCapacity;
        if (utilization > 1.0) {
            int over = allocatedPoints - availableCapacity;
            return new AbstractMap.SimpleEntry<>(true,
                    "Sprint is over capacity by " + over + " points (" + String.format("%.0f", utilization * 100) + "% utilization)");
        }
        if (utilization > 0.9) {
            return new AbstractMap.SimpleEntry<>(false,
                    "Sprint is at " + String.format("%.0f", utilization * 100) + "% capacity - near limit");
        }
        return new AbstractMap.SimpleEntry<>(false, "Capacity is within acceptable range");
    }

    public Map<Long, List<Task>> balanceWorkload(List<Task> tasks, List<SprintCapacity> capacities) {
        Map<Long, List<Task>> assignment = new LinkedHashMap<>();
        for (SprintCapacity cap : capacities) {
            assignment.put(cap.getUserId(), new ArrayList<>());
        }

        Map<Long, Integer> memberLoad = new HashMap<>();
        Map<Long, Integer> memberCapacity = new HashMap<>();
        for (SprintCapacity cap : capacities) {
            int capacityPoints = (int) (cap.getAvailableHours() * POINTS_PER_HOUR);
            memberCapacity.put(cap.getUserId(), Math.max(capacityPoints, 1));
            memberLoad.put(cap.getUserId(), 0);
        }

        List<Task> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((a, b) -> {
            TaskPriority pa = a.getPriority() != null ? a.getPriority() : TaskPriority.LOW;
            TaskPriority pb = b.getPriority() != null ? b.getPriority() : TaskPriority.LOW;
            return Integer.compare(
                    getPriorityWeight(pa),
                    getPriorityWeight(pb)
            );
        });

        for (Task task : sortedTasks) {
            Long assigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;
            int sp = task.getStoryPoints() != null ? task.getStoryPoints() : 1;

            if (assigneeId != null && memberLoad.containsKey(assigneeId)) {
                int currentLoad = memberLoad.get(assigneeId);
                int cap = memberCapacity.get(assigneeId);
                if (currentLoad + sp <= cap) {
                    assignment.get(assigneeId).add(task);
                    memberLoad.put(assigneeId, currentLoad + sp);
                    continue;
                }
            }

            Long bestMember = null;
            int bestLoad = Integer.MAX_VALUE;
            for (Map.Entry<Long, Integer> entry : memberLoad.entrySet()) {
                int cap = memberCapacity.get(entry.getKey());
                if (entry.getValue() + sp <= cap && entry.getValue() < bestLoad) {
                    bestLoad = entry.getValue();
                    bestMember = entry.getKey();
                }
            }
            if (bestMember != null) {
                assignment.get(bestMember).add(task);
                memberLoad.put(bestMember, bestLoad + sp);
            }
        }

        return assignment;
    }

    private int getPriorityWeight(TaskPriority p) {
        if (p == null) return 3;
        return switch (p) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            default -> 3;
        };
    }
}



