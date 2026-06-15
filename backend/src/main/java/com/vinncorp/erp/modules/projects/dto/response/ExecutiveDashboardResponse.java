package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ExecutiveDashboardResponse {
    private int activeProjects;
    private int atRiskProjects;
    private double averageVelocity;
    private double deliveryPredictability;
    private List<ProjectRiskSummary> topRisks;
    private Map<String, Object> trendSummary;
}



