package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import com.vinncorp.erp.modules.projects.mapper.TaskMapper;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.SprintCarryForwardService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintCarryForwardServiceImpl implements SprintCarryForwardService {

    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    @Override
    public List<TaskResponse> getCarryForwardCandidates(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);

        return taskSprints.stream()
                .map(TaskSprint::getTask)
                .filter(Objects::nonNull)
                .filter(task -> task.getStatusEntity() == null
                        || !isDoneStatus(task.getStatusEntity().getName()))
                .map(TaskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getDependencyAwareRollover(Long sprintId) {
        List<TaskResponse> candidates = getCarryForwardCandidates(sprintId);

        Set<Long> candidateIds = candidates.stream()
                .map(TaskResponse::getId)
                .collect(Collectors.toSet());

        List<Long> depIds = new ArrayList<>();
        for (TaskResponse task : candidates) {
            List<TaskDependency> deps = taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(task.getId());
            for (TaskDependency dep : deps) {
                if (dep.getDependsOnTask() != null
                        && !candidateIds.contains(dep.getDependsOnTask().getId())
                        && !isDoneStatus(dep.getDependsOnTask().getStatusEntity() != null
                        ? dep.getDependsOnTask().getStatusEntity().getName() : null)) {
                    depIds.add(dep.getDependsOnTask().getId());
                }
            }
        }

        if (!depIds.isEmpty()) {
            List<Task> depTasks = taskRepository.findAllById(depIds);
            for (Task t : depTasks) {
                candidates.add(TaskMapper.toResponse(t));
            }
        }

        return candidates;
    }

    @Override
    public List<TaskResponse> getBlockedTaskPriorities(Long sprintId) {
        List<TaskResponse> candidates = getCarryForwardCandidates(sprintId);

        List<TaskResponse> blocked = new ArrayList<>();
        for (TaskResponse task : candidates) {
            List<TaskDependency> deps = taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(task.getId());
            boolean isBlocked = deps.stream().anyMatch(d ->
                    d.getDependencyType() == DependencyType.BLOCKED_BY
                            && d.getDependsOnTask() != null
                            && d.getDependsOnTask().getStatusEntity() != null
                            && !isDoneStatus(d.getDependsOnTask().getStatusEntity().getName()));
            if (isBlocked) {
                blocked.add(task);
            }
        }

        blocked.sort((a, b) -> {
            int aBlocked = countBlockers(a.getId());
            int bBlocked = countBlockers(b.getId());
            return Integer.compare(bBlocked, aBlocked);
        });

        return blocked;
    }

    private int countBlockers(Long taskId) {
        List<TaskDependency> deps = taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(taskId);
        return (int) deps.stream().filter(d ->
                d.getDependsOnTask() != null
                        && d.getDependsOnTask().getStatusEntity() != null
                        && !isDoneStatus(d.getDependsOnTask().getStatusEntity().getName())).count();
    }

    private boolean isDoneStatus(String name) {
        if (name == null) return false;
        String n = name.toUpperCase().trim();
        return "DONE".equals(n) || "COMPLETED".equals(n) || "CLOSED".equals(n);
    }
}



