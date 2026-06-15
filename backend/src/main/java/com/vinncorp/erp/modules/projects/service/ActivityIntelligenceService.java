package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.ActivityIntelligenceResponse;

public interface ActivityIntelligenceService {

    ActivityIntelligenceResponse generateSummary(Long workspaceId, Long projectId, int days);
}



