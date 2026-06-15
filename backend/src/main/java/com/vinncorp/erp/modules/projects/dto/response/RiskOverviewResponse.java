package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Workspace-level risk overview")
public class RiskOverviewResponse {
    private Long workspaceId;
    private int totalProjects;
    private int atRiskProjects;
    private int criticalProjects;
    private double averageRiskScore;
    private List<ProjectRiskSummary> projects;
}



