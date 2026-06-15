package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Spillover prediction for a sprint")
public class SpilloverPredictionResponse {
    private Long sprintId;
    private String sprintName;
    private int predictedSpilloverPoints;
    private double spilloverProbability;
    private List<String> atRiskTasks;
}



