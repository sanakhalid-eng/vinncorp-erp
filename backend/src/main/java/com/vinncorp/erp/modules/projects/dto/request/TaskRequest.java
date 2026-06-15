package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Task creation/update request")
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    @Schema(example = "Implement login", description = "Task title")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    @Schema(example = "Implement JWT-based login for the application", description = "Task description")
    private String description;

    @NotNull(message = "Priority is required")
    @Schema(example = "HIGH", description = "Task priority")
    private TaskPriority priority;

    @Schema(example = "1", description = "Status ID")
    private Long statusId;

    @Schema(example = "2025-12-31T23:59:59", description = "Due date")
    private LocalDateTime dueDate;

    @Schema(description = "Start date for Gantt timeline")
    private LocalDateTime startDate;

    @Schema(description = "End date for Gantt timeline")
    private LocalDateTime endDate;

    @NotNull(message = "Project ID is required")
    @Schema(example = "1", description = "Project ID")
    private Long projectId;

    @Schema(example = "2", description = "Assignee user ID")
    private Long assigneeId;

    @Schema(example = "1", description = "Column/board column ID")
    private Long columnId;

    @Schema(example = "0", description = "Position within the column")
    private Integer position;
}



