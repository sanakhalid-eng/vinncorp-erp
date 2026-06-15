package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.SprintBurndown;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.repository.SprintBurndownRepository;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final SprintRepository sprintRepository;
    private final SprintBurndownRepository sprintBurndownRepository;
    private final BurndownService burndownService;
    private final TimeLogRepository timeLogRepository;

    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse getDashboardSummary(Long projectId) {
        AnalyticsDashboardResponse response = new AnalyticsDashboardResponse();

        // Get completed sprints sorted by completion date
        List<Sprint> completedSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.COMPLETED.name())
                .stream()
                .sorted(Comparator.comparing(Sprint::getCompletedAt))
                .collect(Collectors.toList());

        // Velocity data
        AnalyticsDashboardResponse.VelocityData velocityData = getVelocityData(completedSprints);
        response.setVelocity(velocityData);

        // Completion rate (based on last completed sprint or average)
        Integer completionRate = getCompletionRate(completedSprints);
        response.setCompletionRate(completionRate);

        // Forecast
        ForecastResponse forecast = getForecast(projectId, velocityData.getAverage());
        response.setForecast(forecast);

        // Blocked work ratio (from last completed sprint)
        Integer blockedWorkRatio = getBlockedWorkRatio(completedSprints);
        response.setBlockedWorkRatio(blockedWorkRatio);

        // Burndown insight
        BurndownInsightResponse burndownInsight = getBurndownInsight(completedSprints);
        response.setBurndownInsight(burndownInsight);

        return response;
    }

    private AnalyticsDashboardResponse.VelocityData getVelocityData(List<Sprint> completedSprints) {
        AnalyticsDashboardResponse.VelocityData velocityData = new AnalyticsDashboardResponse.VelocityData();

        List<VelocityHistoryResponse> history = completedSprints.stream()
                .filter(s -> s.getSummaryCompletedTasks() != null)
                .map(s -> {
                    VelocityHistoryResponse v = new VelocityHistoryResponse();
                    v.setSprintId(s.getId());
                    v.setSprintName(s.getName());
                    v.setCompletedTasks(s.getSummaryCompletedTasks());
                    v.setCompletedAt(s.getCompletedAt() != null ? s.getCompletedAt().toLocalDate().toString() : null);
                    return v;
                })
                .collect(Collectors.toList());

        velocityData.setHistory(history);

        // Calculate average from last 5 sprints (or all if less than 5)
        int sprintsToConsider = Math.min(5, history.size());
        if (sprintsToConsider > 0) {
            Double avg = history.subList(history.size() - sprintsToConsider, history.size()).stream()
                    .mapToInt(VelocityHistoryResponse::getCompletedTasks)
                    .average()
                    .orElse(0.0);
            velocityData.setAverage(avg);
        } else {
            velocityData.setAverage(0.0);
        }

        // Determine trend
        VelocityTrendResponse trend = getVelocityTrend(history);
        velocityData.setTrend(trend.getTrend());
        velocityData.setChangePercentage(trend.getChangePercentage());

        return velocityData;
    }

    private VelocityTrendResponse getVelocityTrend(List<VelocityHistoryResponse> history) {
        VelocityTrendResponse response = new VelocityTrendResponse();
        response.setTrend("STABLE");
        response.setChangePercentage(0.0);

        if (history.size() < 2) {
            return response;
        }

        // Compare first and last sprint velocities
        int firstTasks = history.getFirst().getCompletedTasks();
        int lastTasks = history.getLast().getCompletedTasks();

        if (firstTasks == 0) {
            if (lastTasks > 0) {
                response.setTrend("INCREASING");
                response.setChangePercentage(100.0);
            }
            return response;
        }

        double change = ((double) (lastTasks - firstTasks) / firstTasks) * 100;

        if (change > 10) {
            response.setTrend("INCREASING");
        } else if (change < -10) {
            response.setTrend("DECREASING");
        } else {
            response.setTrend("STABLE");
        }

        response.setChangePercentage(Math.round(change * 10.0) / 10.0);
        return response;
    }

    private Integer getCompletionRate(List<Sprint> completedSprints) {
        if (completedSprints.isEmpty()) {
            return 0;
        }

        // Use the most recent completed sprint
        Sprint latest = completedSprints.getLast();
        if (latest.getSummaryTotalTasks() != null && latest.getSummaryTotalTasks() > 0) {
            return (int) Math.round(
                    (latest.getSummaryCompletedTasks() * 100.0) / latest.getSummaryTotalTasks()
            );
        }
        return 0;
    }

    private ForecastResponse getForecast(Long projectId, Double averageVelocity) {
        ForecastResponse response = new ForecastResponse();

        // Get active sprint to determine remaining tasks
        Sprint activeSprint = sprintRepository.findActiveSprintByProjectId(projectId).orElse(null);
        int remainingTasks = 0;

        if (activeSprint != null) {
            // Get current burndown data
            java.util.List<SprintBurndown> burndownData = burndownService.getBurndownData(activeSprint.getId());
            if (!burndownData.isEmpty()) {
                // Get the latest snapshot
                SprintBurndown latest = burndownData.getLast();
                remainingTasks = latest.getRemainingTasks();
            } else {
                remainingTasks = activeSprint.getSummaryTotalTasks() != null
                        ? activeSprint.getSummaryTotalTasks() - (activeSprint.getSummaryCompletedTasks() != null ? activeSprint.getSummaryCompletedTasks() : 0)
                        : 0;
            }
        }

        response.setRemainingTasks(remainingTasks);
        response.setAverageVelocity(averageVelocity != null ? averageVelocity.intValue() : 0);

        if (averageVelocity != null && averageVelocity > 0) {
            double estimatedSprints = Math.ceil(remainingTasks / averageVelocity);
            response.setEstimatedSprints(estimatedSprints);
        } else {
            response.setEstimatedSprints(0.0);
        }

        return response;
    }

    private Integer getBlockedWorkRatio(List<Sprint> completedSprints) {
        if (completedSprints.isEmpty()) {
            return 0;
        }

        // Use the most recent completed sprint
        Sprint latest = completedSprints.getLast();

        if (latest.getSummaryTotalTasks() != null && latest.getSummaryTotalTasks() > 0) {
            // We need to get blocked tasks from burndown data
            java.util.List<SprintBurndown> burndownData = burndownService.getBurndownData(latest.getId());
            if (!burndownData.isEmpty()) {
                SprintBurndown latestSnapshot = burndownData.get(burndownData.size() - 1);
                return (int) Math.round(
                        (latestSnapshot.getBlockedTasks() * 100.0) / latest.getSummaryTotalTasks()
                );
            }
        }
        return 0;
    }

    private BurndownInsightResponse getBurndownInsight(List<Sprint> completedSprints) {
        BurndownInsightResponse response = new BurndownInsightResponse();
        response.setStatus("ON_TRACK");
        response.setAverageDeviationFromIdeal(0.0);
        response.setTotalDataPoints(0);
        response.setBehindScheduleCount(0);

        if (completedSprints.isEmpty()) {
            return response;
        }

        // Analyze the most recent completed sprint
        Sprint latest = completedSprints.get(completedSprints.size() - 1);
        java.util.List<SprintBurndown> burndownData = burndownService.getBurndownData(latest.getId());

        if (burndownData.size() < 2) {
            return response;
        }

        int totalPoints = burndownData.size();
        int behindCount = 0;
        double totalDeviation = 0;

        for (int i = 0; i < burndownData.size(); i++) {
            SprintBurndown point = burndownData.get(i);
            if (point.getTotalTasks() == 0) continue;

            double idealPerDay = (double) point.getTotalTasks() / burndownData.size();
            double idealRemaining = point.getTotalTasks() - (idealPerDay * i);
            double actualRemaining = point.getRemainingTasks();

            double deviation = actualRemaining - idealRemaining;
            totalDeviation += deviation;

            if (deviation > 0) {
                behindCount++;
            }
        }

        double avgDeviation = totalDeviation / totalPoints;

        if (behindCount > totalPoints / 2) {
            response.setStatus("BEHIND_SCHEDULE");
        } else if (behindCount == 0) {
            response.setStatus("AHEAD_OF_SCHEDULE");
        } else {
            response.setStatus("ON_TRACK");
        }

        response.setAverageDeviationFromIdeal(Math.round(avgDeviation * 10.0) / 10.0);
        response.setTotalDataPoints(totalPoints);
        response.setBehindScheduleCount(behindCount);

        return response;
    }

    /**
     * Get time-based analytics for a project
     * Returns average time per task, total hours logged, and time trends
     */
    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse.TimeAnalytics getTimeAnalytics(Long projectId) {
        AnalyticsDashboardResponse.TimeAnalytics timeAnalytics = new AnalyticsDashboardResponse.TimeAnalytics();

        // Get all tasks for the project with their time logs
        List<Object[]> taskHours = timeLogRepository.getTaskHoursByProjectId(projectId);

        if (taskHours == null || taskHours.isEmpty()) {
            timeAnalytics.setTotalHours(BigDecimal.ZERO);
            timeAnalytics.setAverageHoursPerTask(BigDecimal.ZERO);
            timeAnalytics.setTasksWithTimeCount(0L);
            return timeAnalytics;
        }

        // Calculate total hours
        BigDecimal totalHours = taskHours.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count tasks with time logged
        long tasksWithTime = taskHours.size();

        // Calculate average hours per task
        BigDecimal avgHoursPerTask = totalHours.divide(BigDecimal.valueOf(tasksWithTime), 2, RoundingMode.HALF_UP);

        timeAnalytics.setTotalHours(totalHours.setScale(2, RoundingMode.HALF_UP));
        timeAnalytics.setAverageHoursPerTask(avgHoursPerTask);
        timeAnalytics.setTasksWithTimeCount(tasksWithTime);

        // Get time trend (last 7 days vs previous 7 days)
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate sevenDaysAgo = now.minusDays(7);
        java.time.LocalDate fourteenDaysAgo = now.minusDays(14);

        BigDecimal recentHours = timeLogRepository.getTotalHoursByProjectIdAndDateRange(projectId, sevenDaysAgo, now);
        BigDecimal previousHours = timeLogRepository.getTotalHoursByProjectIdAndDateRange(projectId, fourteenDaysAgo, sevenDaysAgo);

        recentHours = recentHours != null ? recentHours : BigDecimal.ZERO;
        previousHours = previousHours != null ? previousHours : BigDecimal.ZERO;

        timeAnalytics.setRecentHours(recentHours.setScale(2, RoundingMode.HALF_UP));
        timeAnalytics.setPreviousHours(previousHours.setScale(2, RoundingMode.HALF_UP));

        if (previousHours.compareTo(BigDecimal.ZERO) > 0) {
            double changePercent = (recentHours.subtract(previousHours))
                    .divide(previousHours, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            timeAnalytics.setChangePercentage(changePercent);
        } else {
            timeAnalytics.setChangePercentage(recentHours.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);
        }

        return timeAnalytics;
    }
}



