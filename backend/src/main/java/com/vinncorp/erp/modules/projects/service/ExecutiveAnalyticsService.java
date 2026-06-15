package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.ExecutiveDashboardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ExecutiveAnalyticsService {

    ExecutiveDashboardResponse getExecutiveDashboard(Long workspaceId);

    void captureSnapshot(Long workspaceId);

    Page<Map<String, Object>> getTrends(Long workspaceId, Pageable pageable);
}



