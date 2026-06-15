package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.DeliveryPredictabilityResponse;

public interface DeliveryPredictabilityService {

    DeliveryPredictabilityResponse analyze(Long workspaceId, Long projectId);
}



