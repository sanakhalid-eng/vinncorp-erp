package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Sprint recommendation details")
public class SprintRecommendation {
    private Long sprintId;
    private String sprintName;
    private int committedPoints;
    private int availableCapacity;
    private double utilizationPercent;
    private List<SprintTaskRecommendation> tasks;
    private List<String> risks;
}



