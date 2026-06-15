package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Sprint velocity response")
public class VelocityResponse {
    private Long sprintId;
    private String sprintName;
    private int committedPoints;
    private int completedPoints;
    private int spilloverPoints;
    private double completionRate;
    private double velocityScore;
}





