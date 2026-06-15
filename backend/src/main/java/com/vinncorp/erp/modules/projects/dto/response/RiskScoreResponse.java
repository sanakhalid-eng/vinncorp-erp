package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.RiskFactor;
import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Project risk score response")
public class RiskScoreResponse {
    private Long projectId;
    private double overallRiskScore;
    private RiskLevel riskLevel;
    private int delayedTasks;
    private int blockedTasks;
    private double velocityDeclinePercent;
    private String trend;
    private List<RiskFactor> factors;
}



