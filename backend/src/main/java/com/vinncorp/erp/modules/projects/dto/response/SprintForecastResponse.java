package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Sprint forecast response")
public class SprintForecastResponse {
    private Long sprintId;
    private String sprintName;
    private int completedPoints;
    private int remainingPoints;
    private double averageVelocity;
    private double projectedCompletionRate;
    private LocalDate projectedCompletionDate;
    private int projectedSpillover;
    private boolean onTrack;
    private double daysRemaining;
    private double daysElapsed;
    private double velocityAdjustedCompletion;
}



