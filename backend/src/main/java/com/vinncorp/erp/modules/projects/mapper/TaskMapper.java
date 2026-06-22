package com.vinncorp.erp.modules.projects.mapper;
import com.vinncorp.erp.platform.user.dto.response.UserResponse;
import com.vinncorp.erp.modules.projects.dto.response.LabelResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskProjectResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.entity.Task;

import java.util.List;
import java.util.stream.Collectors;

public class TaskMapper {

    public static TaskResponse toResponse(Task task) {

        if (task == null) return null;

        TaskResponse response = new TaskResponse();

        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());

        // -------------------------
        // STATUS (WorkflowStatus)
        // -------------------------
        if (task.getStatusEntity() != null) {
            response.setStatusId(task.getStatusEntity().getId());
            response.setStatus(task.getStatusEntity().getName());
        }

        // -------------------------
        // PRIORITY (SAFE ENUM)
        // -------------------------
        response.setPriority(
                task.getPriority() != null
                        ? task.getPriority().name()
                        : null
        );

        response.setStoryPoints(task.getStoryPoints());
        response.setDueDate(task.getDueDate());
        response.setStartDate(task.getStartDate());
        response.setEndDate(task.getEndDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        // -------------------------
        // PROJECT (SAFE)
        // -------------------------
        if (task.getProject() != null) {
            TaskProjectResponse projectResponse = new TaskProjectResponse();

            projectResponse.setId(task.getProject().getId());
            projectResponse.setName(task.getProject().getName());

            if (task.getProject().getStatus() != null) {
                projectResponse.setStatus(task.getProject().getStatus().getName());
            }

            response.setProject(projectResponse);
        }

        if (task.getCreator() != null) {
            UserResponse createdBy = new UserResponse();
            createdBy.setId(task.getCreator().getId());
            createdBy.setName(task.getCreator().getName());
            createdBy.setEmail(task.getCreator().getEmail());
            response.setCreatedBy(createdBy);
        }

        // -------------------------
        // ASSIGNEE (SAFE)
        // -------------------------
        if (task.getAssignee() != null) {
            UserResponse userResponse = new UserResponse();

            userResponse.setId(task.getAssignee().getId());
            userResponse.setName(task.getAssignee().getName());
            userResponse.setEmail(task.getAssignee().getEmail());

            response.setAssignee(userResponse);
        }

        if (task.getParentTask() != null) {
            response.setParentTaskId(task.getParentTask().getId());
            response.setParentTaskTitle(task.getParentTask().getTitle());
        }

        if (task.getTaskLabels() != null && !task.getTaskLabels().isEmpty()) {
            List<LabelResponse> labels = task.getTaskLabels().stream()
                    .map(tl -> {
                        LabelResponse lr = new LabelResponse();
                        lr.setId(tl.getLabel().getId());
                        lr.setName(tl.getLabel().getName());
                        lr.setColor(tl.getLabel().getColor());
                        return lr;
                    })
                    .collect(Collectors.toList());
            response.setLabels(labels);
        }

        return response;
    }

    public static TaskResponse toResponseWithoutSubtasks(Task task) {
        return toResponse(task);
    }
}



