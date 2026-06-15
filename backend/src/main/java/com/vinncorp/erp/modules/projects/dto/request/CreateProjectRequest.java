package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.ProjectPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Create project request")
public class CreateProjectRequest {

    @Schema(example = "My Project", description = "Project name")
    private String name;

    @Schema(example = "Description of the project", description = "Project description")
    private String description;

    @Schema(example = "2025-01-01T00:00:00", description = "Project start date")
    private LocalDateTime startDate;

    @Schema(example = "2025-12-31T23:59:59", description = "Project end date")
    private LocalDateTime endDate;

    @Schema(example = "HIGH", description = "Project priority")
    private ProjectPriority priority;

    @Schema(example = "backend,frontend", description = "Tags")
    private String tags;

    @Schema(example = "Development", description = "Category")
    private String category;

    @Schema(example = "Build a project management system", description = "Objectives")
    private String objectives;

    @Schema(example = "10000.00", description = "Budget")
    private Double budget;

    @Schema(example = "USD", description = "Currency")
    private String currency;

    @Schema(example = "2", description = "Optional project manager user ID")
    private Long projectManagerId;

    @Schema(example = "true", description = "Whether the project is active")
    private Boolean isActive;

    @Schema(example = "true", description = "Whether the project is public")
    private Boolean isPublic;
}


