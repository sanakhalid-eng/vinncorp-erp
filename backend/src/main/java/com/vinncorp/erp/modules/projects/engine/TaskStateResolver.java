package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.dto.response.LabelResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskStateResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskStateResolver {

    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final WorkflowStatusRepository workflowStatusRepository;

    @Transactional(readOnly = true)
    public TaskStateResponse resolve(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskStateResponse state = new TaskStateResponse();
        state.setTaskId(task.getId());
        state.setStatusId(task.getStatusEntity() != null ? task.getStatusEntity().getId() : null);
        state.setStatus(task.getStatusEntity() != null ? task.getStatusEntity().getName() : null);
        state.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
        state.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        state.setAssigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null);
        state.setProjectId(task.getProject() != null ? task.getProject().getId() : null);

        // Labels - avoid N+1 by using query with fetch join
        if (task.getTaskLabels() != null) {
            List<LabelResponse> labels = task.getTaskLabels().stream()
                    .filter(tl -> tl.getLabel() != null && tl.getLabel().getDeletedAt() == null)
                    .map(tl -> {
                        LabelResponse lr = new LabelResponse();
                        lr.setId(tl.getLabel().getId());
                        lr.setName(tl.getLabel().getName());
                        lr.setColor(tl.getLabel().getColor());
                        return lr;
                    })
                    .collect(Collectors.toList());
            state.setLabels(labels);
        }

        // Sprint state
        TaskSprint taskSprint = taskSprintRepository.findByTaskId(taskId).orElse(null);
        if (taskSprint != null && taskSprint.getSprint() != null) {
            state.setInSprint(true);
            state.setSprintId(taskSprint.getSprint().getId());
            state.setSprintStatus(taskSprint.getSprint().getStatus() != null
                    ? taskSprint.getSprint().getStatus().name() : "NONE");
        } else {
            state.setInSprint(false);
            state.setSprintStatus("NONE");
        }

        // Dependency blocking
        List<TaskDependency> dependencies = taskDependencyRepository.findByTaskId(taskId);
        boolean blocked = false;
        boolean crossSprint = false;

        WorkflowStatus doneStatus = workflowStatusRepository
                .findByIsDefaultTrueAndProjectId(task.getProject().getId())
                .orElse(null);
        Long doneStatusId = doneStatus != null ? doneStatus.getId() : null;

        for (TaskDependency dep : dependencies) {
            if (dep.getDependsOnTask() == null) continue;

            // Check if dependency is incomplete
            if (doneStatusId != null) {
                Long depStatusId = dep.getDependsOnTask().getStatusEntity() != null
                        ? dep.getDependsOnTask().getStatusEntity().getId() : null;
                if (depStatusId == null || !depStatusId.equals(doneStatusId)) {
                    blocked = true;
                }
            }

            // Check cross-sprint dependency
            if (state.getInSprint()) {
                TaskSprint depSprint = taskSprintRepository.findByTaskId(dep.getDependsOnTask().getId()).orElse(null);
                if (depSprint == null || !depSprint.getSprint().getId().equals(state.getSprintId())) {
                    crossSprint = true;
                }
            }
        }

        state.setBlocked(blocked);
        state.setCrossSprintDependency(crossSprint);

        // Subtask progress
        int subtaskCount = task.getSubtaskCount();
        int completedSubtasks = task.getCompletedSubtaskCount();
        state.setHasSubtasks(subtaskCount > 0);
        state.setSubtaskCompletionPercentage(
                subtaskCount > 0 ? Math.round((completedSubtasks * 100.0f) / subtaskCount) : 0
        );

        return state;
    }

    @Transactional(readOnly = true)
    public List<TaskStateResponse> resolveAllByProject(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdIn(List.of(projectId));
        return tasks.stream().map(t -> resolve(t.getId())).collect(Collectors.toList());
    }
}



