package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.RiskOverviewResponse;
import com.vinncorp.erp.modules.projects.dto.response.RiskScoreResponse;
import com.vinncorp.erp.modules.projects.dto.response.SprintRiskAnalysisResponse;

public interface ExecutionRiskService {

    RiskScoreResponse getProjectRiskScore(Long workspaceId, Long projectId);

    SprintRiskAnalysisResponse getSprintRiskAnalysis(Long workspaceId, Long sprintId);

    RiskOverviewResponse getWorkspaceRiskOverview(Long workspaceId);
}



