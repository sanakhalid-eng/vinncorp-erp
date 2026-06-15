package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BurndownService {

    private final SprintBurndownRepository sprintBurndownRepository;
    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final WorkflowStatusRepository workflowStatusRepository;

    @Transactional
    public SprintBurndown computeAndSaveSnapshot(Long sprintId, LocalDate date) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));

        Long doneStatusId = workflowStatusRepository
                .findByIsDefaultTrueAndProjectId(sprint.getProject().getId())
                .map(WorkflowStatus::getId)
                .orElse(null);

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintId(sprintId);
        int totalTasks = taskSprints.size();

        int completedTasks = 0;
        int blockedTasks = 0;

        for (TaskSprint ts : taskSprints) {
            Task task = ts.getTask();
            if (task == null || task.getStatusEntity() == null) continue;

            if (task.getStatusEntity().getId().equals(doneStatusId)) {
                completedTasks++;
            }

            List<TaskDependency> deps = taskDependencyRepository.findByTaskId(task.getId());
            for (TaskDependency dep : deps) {
                if (dep.getDependsOnTask() == null) continue;
                if (doneStatusId == null) continue;
                Long depStatusId = dep.getDependsOnTask().getStatusEntity() != null
                        ? dep.getDependsOnTask().getStatusEntity().getId() : null;
                if (depStatusId == null || !depStatusId.equals(doneStatusId)) {
                    blockedTasks++;
                    break;
                }
            }
        }

        int remainingTasks = totalTasks - completedTasks;

        Optional<SprintBurndown> existing = sprintBurndownRepository.findBySprintIdAndDate(sprintId, date);
        SprintBurndown burndown;

        if (existing.isPresent()) {
            burndown = existing.get();
            burndown.setTotalTasks(totalTasks);
            burndown.setCompletedTasks(completedTasks);
            burndown.setRemainingTasks(remainingTasks);
            burndown.setBlockedTasks(blockedTasks);
        } else {
            burndown = new SprintBurndown();
            burndown.setSprint(sprint);
            burndown.setDate(date);
            burndown.setTotalTasks(totalTasks);
            burndown.setCompletedTasks(completedTasks);
            burndown.setRemainingTasks(remainingTasks);
            burndown.setBlockedTasks(blockedTasks);
        }

        SprintBurndown saved = sprintBurndownRepository.save(burndown);
        log.info("Burndown snapshot saved for sprintId={}, date={}: total={}, completed={}, remaining={}, blocked={}",
                sprintId, date, totalTasks, completedTasks, remainingTasks, blockedTasks);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<SprintBurndown> getBurndownData(Long sprintId) {
        return sprintBurndownRepository.findBySprintIdOrderByDateAsc(sprintId);
    }
}



