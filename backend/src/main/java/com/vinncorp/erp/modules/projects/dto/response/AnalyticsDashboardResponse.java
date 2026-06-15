package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AnalyticsDashboardResponse {
    private VelocityData velocity;
    private Integer completionRate;
    private ForecastResponse forecast;
    private Integer blockedWorkRatio;
    private BurndownInsightResponse burndownInsight;
    private TimeAnalytics timeAnalytics;

    @Data
    public static class VelocityData {
        private List<VelocityHistoryResponse> history;
        private Double average;
        private String trend;
        private Double changePercentage;
    }

    @Data
    public static class TimeAnalytics {
        private BigDecimal totalHours;
        private BigDecimal averageHoursPerTask;
        private Long tasksWithTimeCount;
        private BigDecimal recentHours;
        private BigDecimal previousHours;
        private Double changePercentage;
    }
}



