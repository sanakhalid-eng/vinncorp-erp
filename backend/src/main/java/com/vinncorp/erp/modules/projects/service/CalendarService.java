package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CalendarResponse;
import com.vinncorp.erp.modules.projects.dto.response.SprintCalendarResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskCalendarResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;

    @Transactional(readOnly = true)
    public CalendarResponse getCalendarData(Long projectId) {
        CalendarResponse response = new CalendarResponse();

        // Get tasks with due dates
        List<TaskCalendarResponse> tasks = getTasks(projectId);
        response.setTasks(tasks);

        // Get sprint calendar blocks
        List<SprintCalendarResponse> sprints = getSprintBlocks(projectId);
        response.setSprints(sprints);

        // Count overdue tasks
        int overdueCount = (int) tasks.stream()
                .filter(t -> {
                    if (t.getDueDate() == null) return false;
                    LocalDate dueDate = LocalDate.parse(t.getDueDate());
                    return dueDate.isBefore(LocalDate.now()) && !isDoneStatus(t.getStatus());
                })
                .count();
        response.setOverdueTasksCount(overdueCount);

        return response;
    }

    private List<TaskCalendarResponse> getTasks(Long projectId) {
        // Fetch tasks with assignee and status in one query to avoid N+1
        List<Task> tasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);

        return tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .map(this::mapToTaskCalendarResponse)
                .sorted(Comparator.comparing(TaskCalendarResponse::getDueDate))
                .collect(Collectors.toList());
    }

    private TaskCalendarResponse mapToTaskCalendarResponse(Task task) {
        TaskCalendarResponse response = new TaskCalendarResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDueDate(task.getDueDate() != null ? task.getDueDate().toString() : null);
        response.setPriority(task.getPriority() != null ? task.getPriority().name() : "MEDIUM");
        response.setStatus(task.getStatusEntity() != null ? task.getStatusEntity().getName() : "Unknown");
        response.setAssigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null);
        response.setStatusColor(task.getStatusEntity() != null ? task.getStatusEntity().getColor() : "#6b7280");

        // Get sprint info if assigned
        taskSprintRepository.findByTaskIdWithSprint(task.getId()).ifPresent(ts -> {
            response.setSprintId(ts.getSprint().getId());
            response.setSprintName(ts.getSprint().getName());
        });

        return response;
    }

    private List<SprintCalendarResponse> getSprintBlocks(Long projectId) {
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByStartDateDesc(projectId);

        return sprints.stream()
                .filter(s -> s.getStartDate() != null && s.getEndDate() != null)
                .map(this::mapToSprintCalendarResponse)
                .collect(Collectors.toList());
    }

    private SprintCalendarResponse mapToSprintCalendarResponse(Sprint sprint) {
        SprintCalendarResponse response = new SprintCalendarResponse();
        response.setId(sprint.getId());
        response.setName(sprint.getName());
        response.setStartDate(sprint.getStartDate().toString());
        response.setEndDate(sprint.getEndDate().toString());
        response.setStatus(sprint.getStatus() != null ? sprint.getStatus().name() : "PLANNED");
        // Calculate progress percentage
        double progress = 0.0;
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            progress = 100.0;
        } else if (sprint.getSummaryTotalTasks() != null && sprint.getSummaryTotalTasks() > 0) {
            progress = (sprint.getSummaryCompletedTasks() * 100.0) / sprint.getSummaryTotalTasks();
        }
        response.setProgressPercentage(progress);
        return response;
    }

    private boolean isDoneStatus(String status) {
        if (status == null) return false;
        String normalized = status.toUpperCase().trim();
        return normalized.equals("DONE") || normalized.equals("COMPLETED") || normalized.equals("CLOSED");
    }
}



