package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

@Data
public class BurndownInsightResponse {
    private String status; // AHEAD_OF_SCHEDULE, ON_TRACK, BEHIND_SCHEDULE
    private Double averageDeviationFromIdeal;
    private Integer totalDataPoints;
    private Integer behindScheduleCount;
}



