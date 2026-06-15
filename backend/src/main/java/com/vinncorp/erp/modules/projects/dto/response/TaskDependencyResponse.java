package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.DependencyType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDependencyResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long dependsOnTaskId;
    private String dependsOnTaskTitle;
    private String dependsOnTaskStatus;
    private boolean dependsOnTaskCompleted;
    private DependencyType dependencyType;
    private String description;
    private LocalDateTime createdAt;
}



