package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class SprintTaskSummaryResponse {
    private Long sprintId;
    private String sprintName;
    private int totalTasks;
    private int completedTasks;
    private int blockedTasks;
    private int remainingTasks;
    private List<TaskStateResponse> taskStates;
}



