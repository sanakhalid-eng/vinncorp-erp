package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveTaskRequest {
    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Source column ID is required")
    private Long sourceColumnId;

    @NotNull(message = "Target column ID is required")
    private Long targetColumnId;

    @NotNull(message = "Position is required")
    private Integer position;
}


