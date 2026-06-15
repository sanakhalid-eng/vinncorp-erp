package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskDependencyRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Depends on task ID is required")
    private Long dependsOnTaskId;
}



