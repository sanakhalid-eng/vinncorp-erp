package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Estimation accuracy response")
public class EstimationAccuracyResponse {
    private int totalTasks;
    private int onTarget;
    private int overEstimated;
    private int underEstimated;
    private double averageDrift;
    private double accuracyPercent;
}


