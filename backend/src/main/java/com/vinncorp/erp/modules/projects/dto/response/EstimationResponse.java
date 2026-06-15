package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.EstimationConfidence;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Story point estimation response")
public class EstimationResponse {
    private int recommendedPoints;
    private EstimationConfidence confidence;
    private List<String> reasoningFactors;
    private HistoricalComparison historicalComparison;
    private PredictedCompletion predictedCompletion;

    @Data
    @Builder
    public static class HistoricalComparison {
        private int previousEstimatesCount;
        private double averagePreviousPoints;
        private double accuracyRate;
        private List<SimilarTask> similarTasks;
    }

    @Data
    @Builder
    public static class SimilarTask {
        private Long taskId;
        private String title;
        private int storyPoints;
        private double similarityScore;
    }

    @Data
    @Builder
    public static class PredictedCompletion {
        private int estimatedDays;
        private double confidencePercent;
        private String riskLevel;
    }
}


