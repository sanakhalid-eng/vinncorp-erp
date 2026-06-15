package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Velocity history entry")
public class VelocityHistoryResponse {
    private Long sprintId;
    private String sprintName;
    private int completedTasks;
    private double velocityScore;
    private String completedAt;
}



