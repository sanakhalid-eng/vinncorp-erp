package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Recommended task for sprint")
public class SprintTaskRecommendation {
    private Long taskId;
    private String taskTitle;
    private int storyPoints;
    private Long assigneeId;
    private String assigneeName;
    private String priority;
    private boolean isDependencyConstrained;
}



