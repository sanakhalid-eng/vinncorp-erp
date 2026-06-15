package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Sprint analytics response")
public class SprintAnalyticsResponse {
    private List<BurndownDataPoint> burndown;
    private List<BurnDataPoint> burnup;
    private double averageCompletionRate;
    private int totalDataPoints;
}



