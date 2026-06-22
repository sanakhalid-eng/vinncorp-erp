package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.ExecutiveDashboardResponse;
import com.vinncorp.erp.modules.projects.dto.response.ProjectRiskSummary;
import com.vinncorp.erp.modules.projects.dto.response.RiskOverviewResponse;
import com.vinncorp.erp.modules.projects.dto.response.VelocityTrendResponse;
import com.vinncorp.erp.modules.projects.entity.AnalyticsSnapshot;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.repository.AnalyticsSnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.ExecutionRiskService;
import com.vinncorp.erp.modules.projects.service.ExecutiveAnalyticsService;
import com.vinncorp.erp.modules.projects.service.VelocityService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ExecutiveAnalyticsServiceImpl implements ExecutiveAnalyticsService {

    private static final String SNAPSHOT_TYPE = "EXECUTIVE";

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AnalyticsSnapshotRepository snapshotRepository;
    private final ExecutionRiskService executionRiskService;
    private final VelocityService velocityService;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public ExecutiveDashboardResponse getExecutiveDashboard(Long workspaceId) {
        requireWorkspace(workspaceId);
        List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);
        RiskOverviewResponse risk = executionRiskService.getWorkspaceRiskOverview(workspaceId);

        double velocitySum = 0;
        int velocityCount = 0;
        for (Project p : projects) {
            VelocityTrendResponse trend = velocityService.getProjectVelocityHistory(p.getId());
            if (trend.getAverageVelocity() != null && trend.getAverageVelocity() > 0) {
                velocitySum += trend.getAverageVelocity();
                velocityCount++;
            }
        }
        double avgVelocity = velocityCount > 0 ? velocitySum / velocityCount : 0;

        List<ProjectRiskSummary> topRisks = risk.getProjects() != null
                ? risk.getProjects().stream().limit(5).toList()
                : List.of();

        Map<String, Object> trendSummary = Map.of(
                "averageRiskScore", risk.getAverageRiskScore(),
                "atRiskProjects", risk.getAtRiskProjects(),
                "criticalProjects", risk.getCriticalProjects()
        );

        return ExecutiveDashboardResponse.builder()
                .activeProjects(projects.size())
                .atRiskProjects(risk.getAtRiskProjects())
                .averageVelocity(avgVelocity)
                .deliveryPredictability(Math.max(0, 100 - risk.getAverageRiskScore()))
                .topRisks(topRisks)
                .trendSummary(trendSummary)
                .build();
    }

    @Override
    @Transactional
    public void captureSnapshot(Long workspaceId) {
        ExecutiveDashboardResponse dashboard = getExecutiveDashboard(workspaceId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        AnalyticsSnapshot snapshot = new AnalyticsSnapshot();
        snapshot.setWorkspace(workspace);
        snapshot.setSnapshotType(SNAPSHOT_TYPE);
        try {
            snapshot.setMetricsJson(objectMapper.writeValueAsString(dashboard));
        } catch (JsonProcessingException e) {
            snapshot.setMetricsJson("{}");
        }
        snapshotRepository.save(snapshot);
        cacheService.evict(CacheNames.executive(workspaceId));
        cacheService.evict(CacheNames.analytics(workspaceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getTrends(Long workspaceId, Pageable pageable) {
        requireWorkspace(workspaceId);
        return snapshotRepository
                .findByWorkspaceIdAndSnapshotTypeAndDeletedAtIsNull(workspaceId, SNAPSHOT_TYPE, pageable)
                .map(s -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", s.getId());
                    row.put("capturedAt", s.getCapturedAt());
                    row.put("projectId", s.getProjectId());
                    row.put("metrics", parseMetrics(s.getMetricsJson()));
                    return row;
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMetrics(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private void requireWorkspace(Long workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }
}



