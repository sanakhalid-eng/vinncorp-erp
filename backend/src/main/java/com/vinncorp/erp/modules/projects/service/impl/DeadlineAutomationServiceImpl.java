package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.service.DeadlineAutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadlineAutomationServiceImpl implements DeadlineAutomationService {

    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final SprintRepository sprintRepository;

    @Override
    @Transactional
    public void autoShiftDueDates(Long taskId, int daysToShift) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getDueDate() == null) return;

        shiftTaskDueDate(task, daysToShift);
        log.info("Auto-shifted due date for task {} by {} days", taskId, daysToShift);
    }

    @Override
    @Transactional
    public void rescheduleDependencyChain(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getDueDate() == null) return;

        Set<Long> visited = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();
        queue.add(task);

        while (!queue.isEmpty()) {
            Task current = queue.poll();
            if (visited.contains(current.getId())) continue;
            visited.add(current.getId());

            List<TaskDependency> dependencies = dependencyRepository.findByDependsOnTaskId(current.getId());
            for (TaskDependency dep : dependencies) {
                Task dependent = dep.getTask();
                if (dependent != null && dependent.getDueDate() != null
                        && dependent.getDueDate().isBefore(current.getDueDate())) {
                    long diff = ChronoUnit.DAYS.between(dependent.getDueDate(), current.getDueDate());
                    shiftTaskDueDate(dependent, (int) diff + 1);
                    log.info("Rescheduled dependent task {} due to task {} shift", dependent.getId(), current.getId());
                    queue.add(dependent);
                }
            }
        }
    }

    @Override
    public List<Task> findOverdueTasksForEscalation(Long projectId) {
        return taskRepository.findByProjectIdAndDueDateBeforeAndCompletedFalse(
                projectId, LocalDate.now().atStartOfDay());
    }

    @Override
    @Transactional
    public void adjustRecurringDeadline(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getDueDate() == null) return;

        List<Task> subtasks = taskRepository.findByParentTaskId(taskId);
        Optional<LocalDateTime> maxSubtaskDate = subtasks.stream()
                .map(Task::getDueDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);

        maxSubtaskDate.ifPresent(date -> {
            if (date.isAfter(task.getDueDate())) {
                task.setDueDate(date);
                taskRepository.save(task);
                log.info("Adjusted recurring deadline for task {} to {}", taskId, date);
            }
        });
    }

    @Override
    @Transactional
    public void sprintAwareSchedule(Long taskId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId).orElse(null);
        if (sprint == null) return;

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return;

        if (sprint.getEndDate() != null) {
            task.setDueDate(sprint.getEndDate().atStartOfDay());
            taskRepository.save(task);
            log.info("Set due date for task {} to sprint {} end date {}", taskId, sprintId, sprint.getEndDate());
        }

        var taskSprint = new TaskSprint();
        taskSprint.setTask(task);
        taskSprint.setSprint(sprint);
        taskSprint.setAssignedAt(java.time.LocalDateTime.now());
        taskSprintRepository.save(taskSprint);
    }

    private void shiftTaskDueDate(Task task, int days) {
        if (task.getDueDate() != null) {
            task.setDueDate(task.getDueDate().plusDays(days));
            taskRepository.save(task);
        }
    }
}



