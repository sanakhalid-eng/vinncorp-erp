package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalendarIntelligenceResponse {
    private int overloadDays;
    private int focusBlocksRecommended;
    private List<String> conflictWarnings;
    private List<TaskCalendarResponse> upcomingCritical;
}



