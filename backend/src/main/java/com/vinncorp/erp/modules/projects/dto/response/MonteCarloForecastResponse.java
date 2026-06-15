package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MonteCarloForecastResponse {
    private Long sprintId;
    private Long projectId;
    private int iterations;
    private LocalDate p50CompletionDate;
    private LocalDate p85CompletionDate;
    private LocalDate p95CompletionDate;
    private double meanRemainingPoints;
    private double confidenceScore;
}



