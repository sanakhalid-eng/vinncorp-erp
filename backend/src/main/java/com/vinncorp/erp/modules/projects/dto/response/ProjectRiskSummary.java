package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Summary of a project's risk status")
public class ProjectRiskSummary {
    private Long projectId;
    private String projectName;
    private double riskScore;
    private RiskLevel riskLevel;
    private int delayedTasks;
    private String trend;
}



