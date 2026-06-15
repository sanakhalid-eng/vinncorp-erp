package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Project productivity metrics")
public class ProductivityResponse {
    private Long projectId;
    private double throughput;
    private double averageCycleTime;
    private double averageLeadTime;
    private double blockedTimeHours;
    private double predictabilityScore;
    private String trend;
    private double productivityScore;
}



