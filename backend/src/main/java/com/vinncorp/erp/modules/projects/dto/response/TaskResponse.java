package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.core.user.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Task response payload")
public class TaskResponse {

    @Schema(example = "1", description = "Task ID")
    private Long id;

    @Schema(example = "Implement login", description = "Task title")
    private String title;

    @Schema(example = "Implement JWT-based login for the application", description = "Task description")
    private String description;

    @Schema(example = "1", description = "Status ID")
    private long statusId;

    @Schema(example = "In Progress", description = "Status name")
    private String status;

    @Schema(example = "HIGH", description = "Task priority")
    private String priority;

    @Schema(example = "2025-12-31T23:59:59", description = "Due date")
    private LocalDateTime dueDate;

    @Schema(description = "Start date for Gantt timeline")
    private LocalDateTime startDate;

    @Schema(description = "End date for Gantt timeline")
    private LocalDateTime endDate;

    @Schema(example = "2025-01-01T10:00:00", description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(example = "2025-01-10T10:00:00", description = "Last update timestamp")
    private LocalDateTime updatedAt;

    private TaskProjectResponse project;
    private UserResponse createdBy;
    private UserResponse assignee;

    @Schema(example = "3", description = "Story points estimate")
    private Integer storyPoints;

    @Schema(example = "null", description = "Parent task ID if this is a subtask")
    private Long parentTaskId;

    @Schema(example = "null", description = "Parent task title if this is a subtask")
    private String parentTaskTitle;

    private SubtaskProgressResponse subtaskProgress;
    private List<TaskResponse> subtasks;
    private List<LabelResponse> labels;
}



