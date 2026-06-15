package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskSprintResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long sprintId;
    private String sprintName;
    private LocalDateTime assignedAt;
}



