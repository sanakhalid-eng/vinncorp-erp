package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VelocityPredictionResponse {
    private double currentVelocity;
    private double predictedVelocity;
    private int sprintCount;
    private String trend;
    private double confidencePercent;
    private List<SprintVelocity> sprintVelocities;

    @Data
    @Builder
    public static class SprintVelocity {
        private Long sprintId;
        private String sprintName;
        private int committedPoints;
        private int completedPoints;
        private double completionRate;
    }
}



