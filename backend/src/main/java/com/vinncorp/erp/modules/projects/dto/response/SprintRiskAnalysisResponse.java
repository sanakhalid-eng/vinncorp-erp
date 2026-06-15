package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Sprint-level risk analysis")
public class SprintRiskAnalysisResponse {
    private Long sprintId;
    private String sprintName;
    private double riskScore;
    private RiskLevel riskLevel;
    private int delayedTasks;
    private int blockedTasks;
    private double completionProbability;
    private List<String> recommendations;
}



