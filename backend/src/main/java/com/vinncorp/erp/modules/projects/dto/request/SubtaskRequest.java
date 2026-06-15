package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubtaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    private TaskPriority priority;

    private Long statusId;

    private LocalDateTime dueDate;

    private Long assigneeId;
}



