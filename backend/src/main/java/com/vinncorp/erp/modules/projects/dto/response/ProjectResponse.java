package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.core.user.response.UserResponse;
import com.vinncorp.erp.modules.projects.enums.ProjectPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Project response payload")
public class ProjectResponse {

    @Schema(example = "1", description = "Project ID")
    private Long id;

    @Schema(example = "1", description = "Workspace ID")
    private Long workspaceId;

    @Schema(example = "My Workspace", description = "Workspace name")
    private String workspaceName;

    @Schema(example = "My Project", description = "Project name")
    private String name;

    @Schema(example = "Description of the project", description = "Project description")
    private String description;

    @Schema(example = "1", description = "Status ID")
    private Long statusId;

    @Schema(example = "Active", description = "Status name")
    private String statusName;

    @Schema(example = "2025-01-01T10:00:00", description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(example = "5", description = "Number of members")
    private Integer memberCount;

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

    @Schema(example = "true", description = "Whether the project is active")
    private boolean isActive;

    @Schema(example = "true", description = "Whether the project is public")
    private boolean isPublic;

    private UserResponse owner;
    private UserResponse projectManager;
    private List<ProjectMemberResponse> members;
}



