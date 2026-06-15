package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

@Data
public class ForecastResponse {
    private Integer remainingTasks;
    private Double estimatedSprints;
    private Integer averageVelocity;
}



