package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.ProjectRiskSummary;
import com.vinncorp.erp.modules.projects.dto.response.RiskOverviewResponse;
import com.vinncorp.erp.modules.projects.dto.response.RiskScoreResponse;
import com.vinncorp.erp.modules.projects.dto.response.SprintRiskAnalysisResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.SprintCapacity;
import com.vinncorp.erp.modules.projects.entity.SprintVelocitySnapshot;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.enums.RiskFactor;
import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.ExecutionRiskService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionRiskServiceImpl implements ExecutionRiskService {

    private static final long CACHE_TTL = 300;
    private static final double OVERDUE_WEIGHT = 0.25;
    private static final double BLOCKED_WEIGHT = 0.25;
    private static final double VELOCITY_WEIGHT = 0.25;
    private static final double WORKLOAD_WEIGHT = 0.25;

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final SprintCapacityRepository sprintCapacityRepository;
    private final SprintVelocitySnapshotRepository velocitySnapshotRepository;
    private final ExecutionRiskSnapshotRepository riskSnapshotRepository;
    private final CacheService cacheService;

    @Override
    public RiskScoreResponse getProjectRiskScore(Long workspaceId, Long projectId) {
        String cacheKey = CacheNames.executionRisk(projectId);
        Optional<RiskScoreResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Task> allTasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        if (allTasks.isEmpty()) {
            RiskScoreResponse empty = RiskScoreResponse.builder()
                    .projectId(projectId)
                    .overallRiskScore(0)
                    .riskLevel(RiskLevel.LOW)
                    .delayedTasks(0)
                    .blockedTasks(0)
                    .velocityDeclinePercent(0)
                    .trend("STABLE")
                    .factors(Collections.emptyList())
                    .build();
            cacheService.put(cacheKey, empty, CACHE_TTL);
            return empty;
        }

        int totalTasks = allTasks.size();
        LocalDateTime now = LocalDateTime.now();

        long delayedCount = allTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now)
                        && !isCompleted(t))
                .count();
        double overduePct = totalTasks > 0 ? (double) delayedCount / totalTasks : 0;

        long blockedCount = allTasks.stream()
                .filter(t -> isBlocked(t))
                .count();
        double blockedPct = totalTasks > 0 ? (double) blockedCount / totalTasks : 0;

        double velocityDeclinePct = calculateVelocityDecline(projectId);

        double workloadImbalancePct = calculateWorkloadImbalance(projectId);

        double riskScore = (OVERDUE_WEIGHT * overduePct * 100)
                + (BLOCKED_WEIGHT * blockedPct * 100)
                + (VELOCITY_WEIGHT * velocityDeclinePct)
                + (WORKLOAD_WEIGHT * workloadImbalancePct);

        riskScore = Math.min(riskScore, 100);

        List<RiskFactor> factors = Arrays.asList(
                RiskFactor.builder()
                        .name("Overdue Tasks")
                        .score(overduePct * 100)
                        .severity(severityLevel(overduePct * 100))
                        .description(delayedCount + " of " + totalTasks + " tasks are past due")
                        .build(),
                RiskFactor.builder()
                        .name("Blocked Tasks")
                        .score(blockedPct * 100)
                        .severity(severityLevel(blockedPct * 100))
                        .description(blockedCount + " of " + totalTasks + " tasks are blocked")
                        .build(),
                RiskFactor.builder()
                        .name("Velocity Decline")
                        .score(velocityDeclinePct)
                        .severity(severityLevel(velocityDeclinePct))
                        .description("Velocity has declined by " + String.format("%.1f", velocityDeclinePct) + "%")
                        .build(),
                RiskFactor.builder()
                        .name("Workload Imbalance")
                        .score(workloadImbalancePct)
                        .severity(severityLevel(workloadImbalancePct))
                        .description("Workload imbalance across team members")
                        .build()
        );

        String trend = determineTrend(allTasks);

        RiskScoreResponse response = RiskScoreResponse.builder()
                .projectId(projectId)
                .overallRiskScore(Math.round(riskScore * 100.0) / 100.0)
                .riskLevel(determineRiskLevel(riskScore))
                .delayedTasks((int) delayedCount)
                .blockedTasks((int) blockedCount)
                .velocityDeclinePercent(Math.round(velocityDeclinePct * 100.0) / 100.0)
                .trend(trend)
                .factors(factors)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    @Override
    public SprintRiskAnalysisResponse getSprintRiskAnalysis(Long workspaceId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprintId);
        List<Task> tasks = taskSprints.stream()
                .map(TaskSprint::getTask)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            return SprintRiskAnalysisResponse.builder()
                    .sprintId(sprintId)
                    .sprintName(sprint.getName())
                    .riskScore(0)
                    .riskLevel(RiskLevel.LOW)
                    .delayedTasks(0)
                    .blockedTasks(0)
                    .completionProbability(100)
                    .recommendations(Collections.singletonList("No tasks in sprint"))
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        long delayedCount = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now) && !isCompleted(t))
                .count();
        long blockedCount = tasks.stream()
                .filter(this::isBlocked)
                .count();

        int total = tasks.size();
        double overduePct = (double) delayedCount / total;
        double blockedPct = (double) blockedCount / total;

        double velocityDeclinePct = calculateSprintVelocityDecline(sprint);
        double workloadImbalancePct = calculateSprintWorkloadImbalance(sprintId);

        double riskScore = (OVERDUE_WEIGHT * overduePct * 100)
                + (BLOCKED_WEIGHT * blockedPct * 100)
                + (VELOCITY_WEIGHT * velocityDeclinePct)
                + (WORKLOAD_WEIGHT * workloadImbalancePct);

        riskScore = Math.min(riskScore, 100);
        double completionProbability = Math.max(0, 100 - riskScore);

        List<String> recommendations = new ArrayList<>();
        if (delayedCount > 0) recommendations.add("Address " + delayedCount + " overdue task(s)");
        if (blockedCount > 0) recommendations.add("Resolve " + blockedCount + " blocked task(s)");
        if (velocityDeclinePct > 20) recommendations.add("Investigate velocity decline - " + String.format("%.1f", velocityDeclinePct) + "% drop");
        if (workloadImbalancePct > 30) recommendations.add("Re-balance workload across team members");
        if (completionProbability < 50) recommendations.add("Consider descoping tasks or extending sprint");
        if (recommendations.isEmpty()) recommendations.add("Sprint is on track");

        return SprintRiskAnalysisResponse.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getName())
                .riskScore(Math.round(riskScore * 100.0) / 100.0)
                .riskLevel(determineRiskLevel(riskScore))
                .delayedTasks((int) delayedCount)
                .blockedTasks((int) blockedCount)
                .completionProbability(Math.round(completionProbability * 100.0) / 100.0)
                .recommendations(recommendations)
                .build();
    }

    @Override
    public RiskOverviewResponse getWorkspaceRiskOverview(Long workspaceId) {
        List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);
        if (projects.isEmpty()) {
            return RiskOverviewResponse.builder()
                    .workspaceId(workspaceId)
                    .totalProjects(0)
                    .atRiskProjects(0)
                    .criticalProjects(0)
                    .averageRiskScore(0)
                    .projects(Collections.emptyList())
                    .build();
        }

        List<ProjectRiskSummary> summaries = new ArrayList<>();
        int atRiskCount = 0;
        int criticalCount = 0;
        double totalRiskScore = 0;

        for (Project project : projects) {
            RiskScoreResponse risk = getProjectRiskScore(workspaceId, project.getId());
            totalRiskScore += risk.getOverallRiskScore();

            if (risk.getRiskLevel() == RiskLevel.HIGH || risk.getRiskLevel() == RiskLevel.CRITICAL) {
                atRiskCount++;
            }
            if (risk.getRiskLevel() == RiskLevel.CRITICAL) {
                criticalCount++;
            }

            summaries.add(ProjectRiskSummary.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .riskScore(risk.getOverallRiskScore())
                    .riskLevel(risk.getRiskLevel())
                    .delayedTasks(risk.getDelayedTasks())
                    .trend(risk.getTrend())
                    .build());
        }

        summaries.sort((a, b) -> Double.compare(b.getRiskScore(), a.getRiskScore()));

        return RiskOverviewResponse.builder()
                .workspaceId(workspaceId)
                .totalProjects(projects.size())
                .atRiskProjects(atRiskCount)
                .criticalProjects(criticalCount)
                .averageRiskScore(projects.size() > 0 ? totalRiskScore / projects.size() : 0)
                .projects(summaries)
                .build();
    }

    private boolean isCompleted(Task task) {
        if (task.getStatusEntity() == null) return false;
        String status = task.getStatusEntity().getName().toUpperCase().trim();
        return "DONE".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status);
    }

    @SuppressWarnings("unused")
    private boolean isBlocked(Task task) {
        if (task.getStatusEntity() == null) return false;
        String status = task.getStatusEntity().getName().toUpperCase().trim();
        return "BLOCKED".equals(status);
    }

    private double calculateVelocityDecline(Long projectId) {
        List<SprintVelocitySnapshot> snapshots = velocitySnapshotRepository
                .findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.size() < 2) return 0;
        double recentAvg = snapshots.stream().limit(Math.min(2, snapshots.size()))
                .mapToDouble(SprintVelocitySnapshot::getVelocityScore)
                .average().orElse(0);
        double olderAvg = snapshots.stream().skip(Math.max(0, snapshots.size() - 2))
                .mapToDouble(SprintVelocitySnapshot::getVelocityScore)
                .average().orElse(0);
        if (olderAvg <= 0) return 0;
        double decline = ((olderAvg - recentAvg) / olderAvg) * 100;
        return Math.max(0, decline);
    }

    private double calculateSprintVelocityDecline(Sprint sprint) {
        Project project = sprint.getProject();
        if (project == null) return 0;
        List<Sprint> completedSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(project.getId(), SprintStatus.COMPLETED.name());
        if (completedSprints.size() < 2) return 0;
        List<SprintVelocitySnapshot> snapshots = velocitySnapshotRepository
                .findByProjectIdOrderByCreatedAtDesc(project.getId());
        if (snapshots.size() < 2) return 0;
        double recent = snapshots.get(0).getVelocityScore();
        double older = snapshots.get(snapshots.size() - 1).getVelocityScore();
        if (older <= 0) return 0;
        return Math.max(0, ((older - recent) / older) * 100);
    }

    private double calculateWorkloadImbalance(Long projectId) {
        List<Sprint> activeSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.ACTIVE.name());
        if (activeSprints.isEmpty()) return 0;
        return calculateSprintWorkloadImbalance(activeSprints.get(0).getId());
    }

    private double calculateSprintWorkloadImbalance(Long sprintId) {
        List<SprintCapacity> capacities = sprintCapacityRepository.findBySprintId(sprintId);
        if (capacities.size() < 2) return 0;
        DoubleSummaryStatistics stats = capacities.stream()
                .mapToDouble(SprintCapacity::getUtilizationPercent)
                .summaryStatistics();
        double range = stats.getMax() - stats.getMin();
        return Math.min(range, 100);
    }

    private String determineTrend(List<Task> tasks) {
        if (tasks.size() < 5) return "STABLE";
        long overdueCount = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now()) && !isCompleted(t))
                .count();
        double overduePct = (double) overdueCount / tasks.size();
        if (overduePct > 0.3) return "DETERIORATING";
        if (overduePct > 0.15) return "WORSENING";
        return "STABLE";
    }

    private String severityLevel(double score) {
        if (score >= 50) return "CRITICAL";
        if (score >= 30) return "HIGH";
        if (score >= 15) return "MEDIUM";
        return "LOW";
    }

    private RiskLevel determineRiskLevel(double score) {
        if (score >= 70) return RiskLevel.CRITICAL;
        if (score >= 40) return RiskLevel.HIGH;
        if (score >= 20) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}



