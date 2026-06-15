package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.EstimationAccuracyResponse;
import com.vinncorp.erp.modules.projects.dto.response.EstimationResponse;
import com.vinncorp.erp.modules.projects.dto.response.VelocityPredictionResponse;

public interface EstimationService {
    EstimationResponse getTaskEstimate(Long workspaceId, Long taskId);
    EstimationAccuracyResponse getProjectEstimationAccuracy(Long workspaceId, Long projectId);
    VelocityPredictionResponse getVelocityPrediction(Long workspaceId, Long projectId);
    EstimationResponse getSimilarEstimates(Long workspaceId, Long taskId);
}



