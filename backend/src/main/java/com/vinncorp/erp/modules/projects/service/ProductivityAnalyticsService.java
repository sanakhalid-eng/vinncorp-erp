package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CycleTimeResponse;
import com.vinncorp.erp.modules.projects.dto.response.ProductivityResponse;
import com.vinncorp.erp.modules.projects.dto.response.TeamHeatmapResponse;
import com.vinncorp.erp.modules.projects.dto.response.ThroughputResponse;

public interface ProductivityAnalyticsService {

    ProductivityResponse getProductivity(Long workspaceId, Long projectId);

    ThroughputResponse getThroughput(Long workspaceId, Long projectId);

    CycleTimeResponse getCycleTime(Long workspaceId, Long projectId);

    TeamHeatmapResponse getTeamHeatmap(Long workspaceId, Long projectId);
}



