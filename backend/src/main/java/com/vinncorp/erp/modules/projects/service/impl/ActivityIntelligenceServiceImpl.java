package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.ActivityIntelligenceResponse;
import com.vinncorp.erp.modules.projects.entity.ActivityIntelligenceSummary;
import com.vinncorp.erp.modules.projects.entity.ActivityLog;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.repository.ActivityIntelligenceSummaryRepository;
import com.vinncorp.erp.modules.projects.repository.ActivityLogRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.ActivityIntelligenceService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityIntelligenceServiceImpl implements ActivityIntelligenceService {

    private static final String SUMMARY_TYPE = "PERIOD_SUMMARY";

    private final ActivityLogRepository activityLogRepository;
    private final ActivityIntelligenceSummaryRepository summaryRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    @Override
    @Transactional
    public ActivityIntelligenceResponse generateSummary(Long workspaceId, Long projectId, int days) {
        requireWorkspace(workspaceId);
        if (projectId != null) {
            requireProjectInWorkspace(workspaceId, projectId);
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(Math.max(1, days));
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay();

        var page = activityLogRepository.findByFilters(
                null, null, null, projectId, workspaceId, startDt, endDt, false, PageRequest.of(0, 500));
        List<ActivityLog> logs = page.getContent();

        Map<String, Long> actionCounts = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getAction().name(), Collectors.counting()));
        long taskUpdates = actionCounts.getOrDefault(ActionType.UPDATED.name(), 0L);
        long creates = actionCounts.getOrDefault(ActionType.CREATED.name(), 0L);

        List<String> highlights = new ArrayList<>();
        highlights.add(String.format("%d activities in the last %d days", logs.size(), days));
        if (creates > 0) highlights.add(creates + " items created");
        if (taskUpdates > 0) highlights.add(taskUpdates + " updates recorded");

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalActivities", logs.size());
        metrics.put("uniqueUsers", logs.stream().map(l -> l.getUser() != null ? l.getUser().getId() : null)
                .filter(Objects::nonNull).distinct().count());
        metrics.put("actionBreakdown", actionCounts);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        ActivityIntelligenceSummary summary = new ActivityIntelligenceSummary();
        summary.setWorkspace(workspace);
        summary.setProjectId(projectId);
        summary.setPeriodStart(start);
        summary.setPeriodEnd(end);
        summary.setSummaryType(SUMMARY_TYPE);
        summary.setHighlightsJson(writeJson(highlights));
        summary.setMetricsJson(writeJson(metrics));
        summaryRepository.save(summary);
        cacheService.evict(CacheNames.activityIntelligence(workspaceId));
        cacheService.evict(CacheNames.activities(workspaceId));

        return ActivityIntelligenceResponse.builder()
                .projectId(projectId)
                .periodStart(start)
                .periodEnd(end)
                .summaryType(SUMMARY_TYPE)
                .highlights(highlights)
                .metrics(metrics)
                .build();
    }

    private void requireWorkspace(Long workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    private void requireProjectInWorkspace(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!workspaceId.equals(project.getWorkspace().getId())) {
            throw new ResourceNotFoundException("Project not found");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}



