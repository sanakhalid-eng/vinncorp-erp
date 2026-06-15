package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CapacityForecastResponse {
    private Long projectId;
    private Long sprintId;
    private double predictedUtilization;
    private int predictedOverloadMembers;
    private double recommendedCapacityHours;
    private int forecastHorizonDays;
}



