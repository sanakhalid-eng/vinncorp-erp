package com.vinncorp.erp.modules.projects.mapper;

import com.vinncorp.erp.modules.projects.dto.response.TaskDependencyResponse;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;

public class TaskDependencyMapper {
    public static TaskDependencyResponse toResponse(TaskDependency dependency) {
        if (dependency == null) return null;

        TaskDependencyResponse response = new TaskDependencyResponse();
        response.setId(dependency.getId());

        if (dependency.getTask() != null) {
            response.setTaskId(dependency.getTask().getId());
            response.setTaskTitle(dependency.getTask().getTitle());
        }

        if (dependency.getDependsOnTask() != null) {
            response.setDependsOnTaskId(dependency.getDependsOnTask().getId());
            response.setDependsOnTaskTitle(dependency.getDependsOnTask().getTitle());
            if (dependency.getDependsOnTask().getStatusEntity() != null) {
                response.setDependsOnTaskStatus(dependency.getDependsOnTask().getStatusEntity().getName());
                String normalized = dependency.getDependsOnTask().getStatusEntity().getName().toUpperCase().trim();
                response.setDependsOnTaskCompleted(
                        normalized.equals("DONE") || normalized.equals("COMPLETED") || normalized.equals("CLOSED")
                );
            }
        }

        response.setDependencyType(dependency.getDependencyType());
        response.setDescription(dependency.getDescription());
        response.setCreatedAt(dependency.getCreatedAt());
        return response;
    }
}



