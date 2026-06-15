package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.SprintHealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Sprint health response")
public class SprintHealthResponse {
    private double score;
    private SprintHealthStatus status;
    private List<String> issues;
    private int overdueTasks;
    private int blockedTasks;
    private double spilloverTrend;
    private double completionRate;
    private int overloadedMembers;
    private int unresolvedCriticalTasks;
}



