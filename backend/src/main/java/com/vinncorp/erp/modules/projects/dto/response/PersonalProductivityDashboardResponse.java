package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PersonalProductivityDashboardResponse {
    private int tasksCompletedThisWeek;
    private int tasksDueThisWeek;
    private int overdueTasks;
    private double focusScore;
    private List<String> topPriorities;
}



