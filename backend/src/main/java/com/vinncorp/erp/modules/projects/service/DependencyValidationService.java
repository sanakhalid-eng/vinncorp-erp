package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.shared.exception.DependencyCycleException;
import com.vinncorp.erp.shared.exception.DependencyValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DependencyValidationService {

    private static final int MAX_CYCLE_DEPTH = 100;
    private static final int MAX_TRAVERSAL_NODES = 10000;

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;

    public void validateNewDependency(Task task, Task dependsOnTask, DependencyType type) {
        validateSelfDependency(task.getId(), dependsOnTask.getId());
        validateCrossProject(task, dependsOnTask);
        validateDuplicate(task.getId(), dependsOnTask.getId(), type);
        validateCycle(task.getId(), dependsOnTask.getId());
        validateAllTasksActive(task.getId(), dependsOnTask.getId());
    }

    public void validateSelfDependency(Long taskId, Long dependsOnTaskId) {
        if (taskId.equals(dependsOnTaskId)) {
            throw new DependencyValidationException("A task cannot depend on itself");
        }
    }

    public void validateCrossProject(Task task, Task dependsOnTask) {
        if (!task.getProject().getId().equals(dependsOnTask.getProject().getId())) {
            throw new DependencyValidationException("Dependencies must be within the same project");
        }
    }

    public void validateDuplicate(Long taskId, Long dependsOnTaskId, DependencyType type) {
        if (type.isSymmetric()) {
            if (taskDependencyRepository.existsByTaskIdAndDependsOnTaskIdAndDeletedAtIsNull(taskId, dependsOnTaskId) ||
                taskDependencyRepository.existsByTaskIdAndDependsOnTaskIdAndDeletedAtIsNull(dependsOnTaskId, taskId)) {
                throw new DependencyValidationException("Dependency already exists between these tasks");
            }
        } else {
            if (taskDependencyRepository.existsByTaskIdAndDependsOnTaskIdAndDeletedAtIsNull(taskId, dependsOnTaskId)) {
                throw new DependencyValidationException("This dependency already exists");
            }
        }
    }

    public void validateCycle(Long sourceTaskId, Long targetTaskId) {
        if (wouldCreateCycle(sourceTaskId, targetTaskId)) {
            throw new DependencyCycleException(sourceTaskId, targetTaskId);
        }
    }

    public void validateAllTasksActive(Long taskId, Long dependsOnTaskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        Task dependsOn = taskRepository.findById(dependsOnTaskId).orElse(null);
        if (task != null && task.isDeleted()) {
            throw new DependencyValidationException("Cannot add dependency: source task is deleted");
        }
        if (dependsOn != null && dependsOn.isDeleted()) {
            throw new DependencyValidationException("Cannot add dependency: target task is deleted");
        }
    }

    public boolean wouldCreateCycle(Long sourceTaskId, Long targetTaskId) {
        return checkCycleBfs(targetTaskId, sourceTaskId);
    }

    private boolean checkCycleBfs(Long startTaskId, Long targetTaskId) {
        Deque<Long> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        queue.addLast(startTaskId);
        visited.add(startTaskId);
        int nodesVisited = 0;

        while (!queue.isEmpty()) {
            if (nodesVisited++ > MAX_TRAVERSAL_NODES) {
                log.warn("Cycle detection exceeded max traversal limit of {} nodes", MAX_TRAVERSAL_NODES);
                return true;
            }

            Long current = queue.removeFirst();
            if (current.equals(targetTaskId)) return true;

            List<TaskDependency> deps = taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(current);
            for (TaskDependency dep : deps) {
                if (dep.getDependsOnTask() != null) {
                    Long nextId = dep.getDependsOnTask().getId();
                    if (!visited.contains(nextId)) {
                        visited.add(nextId);
                        queue.addLast(nextId);
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCircularDependency(Long taskId) {
        return checkCycleBfs(taskId, taskId);
    }
}



