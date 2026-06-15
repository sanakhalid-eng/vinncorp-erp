package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.GanttDataResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GanttService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    public GanttDataResponse getGanttData(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByStartDateDesc(projectId);

        List<TaskDependency> deps = tasks.stream()
                .flatMap(t -> taskDependencyRepository.findByTaskId(t.getId()).stream())
                .filter(d -> !d.isDeleted())
                .toList();

        List<GanttDataResponse.GanttTask> ganttTasks = tasks.stream()
                .map(this::toGanttTask)
                .collect(Collectors.toList());

        List<GanttDataResponse.GanttSprint> ganttSprints = sprints.stream()
                .map(this::toGanttSprint)
                .collect(Collectors.toList());

        List<GanttDataResponse.GanttDependency> ganttDeps = deps.stream()
                .map(this::toGanttDependency)
                .collect(Collectors.toList());

        return GanttDataResponse.builder()
                .tasks(ganttTasks)
                .sprints(ganttSprints)
                .dependencies(ganttDeps)
                .build();
    }

    private GanttDataResponse.GanttTask toGanttTask(Task task) {
        int totalSubtasks = task.getSubtaskCount();
        int completedSubtasks = task.getCompletedSubtaskCount();
        double progress = totalSubtasks > 0 ? (double) completedSubtasks / totalSubtasks * 100 : 0;

        return GanttDataResponse.GanttTask.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .dueDate(task.getDueDate())
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .status(task.getStatusEntity() != null ? task.getStatusEntity().getName() : null)
                .statusColor(task.getStatusEntity() != null ? task.getStatusEntity().getColor() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .parentTaskId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                .storyPoints(task.getStoryPoints())
                .progress(progress)
                .build();
    }

    private GanttDataResponse.GanttSprint toGanttSprint(Sprint sprint) {
        double progress = 0.0;
        if (sprint.getSummaryTotalTasks() != null && sprint.getSummaryTotalTasks() > 0) {
            progress = (sprint.getSummaryCompletedTasks() != null ? sprint.getSummaryCompletedTasks() : 0) * 100.0 / sprint.getSummaryTotalTasks();
        }
        return GanttDataResponse.GanttSprint.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .startDate(sprint.getStartDate() != null ? sprint.getStartDate().atStartOfDay() : null)
                .endDate(sprint.getEndDate() != null ? sprint.getEndDate().atStartOfDay() : null)
                .status(sprint.getStatus() != null ? sprint.getStatus().name() : null)
                .progressPercentage(progress)
                .build();
    }

    private GanttDataResponse.GanttDependency toGanttDependency(TaskDependency dep) {
        return GanttDataResponse.GanttDependency.builder()
                .id(dep.getId())
                .sourceTaskId(dep.getDependsOnTask() != null ? dep.getDependsOnTask().getId() : null)
                .targetTaskId(dep.getTask() != null ? dep.getTask().getId() : null)
                .dependencyType(dep.getDependencyType() != null ? dep.getDependencyType().name() : null)
                .build();
    }
}



