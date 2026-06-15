package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.SprintPlanRequest;
import com.vinncorp.erp.modules.projects.dto.response.CapacityRiskResponse;
import com.vinncorp.erp.modules.projects.dto.response.SpilloverPredictionResponse;
import com.vinncorp.erp.modules.projects.dto.response.SprintPlanResponse;
import com.vinncorp.erp.modules.projects.dto.response.SprintRecommendation;

import java.util.List;

public interface SprintPlanningService {

    SprintPlanResponse planSprint(Long workspaceId, Long projectId, SprintPlanRequest request);

    List<SprintRecommendation> getSprintRecommendations(Long workspaceId, Long projectId);

    CapacityRiskResponse getCapacityRisks(Long workspaceId, Long sprintId);

    SpilloverPredictionResponse getSpilloverPrediction(Long workspaceId, Long projectId);
}



