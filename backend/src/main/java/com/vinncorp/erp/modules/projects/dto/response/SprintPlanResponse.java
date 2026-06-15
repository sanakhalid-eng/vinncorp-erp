package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Sprint planning response with recommendations")
public class SprintPlanResponse {
    private List<SprintRecommendation> recommendations;
    private int totalTasks;
    private int totalPoints;
    private int availableCapacity;
    private boolean isOverCapacity;
    private List<String> warnings;
}


