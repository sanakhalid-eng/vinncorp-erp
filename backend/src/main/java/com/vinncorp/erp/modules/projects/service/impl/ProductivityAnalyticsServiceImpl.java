package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.entity.User;

import com.vinncorp.erp.modules.projects.dto.response.CycleTimeResponse;
import com.vinncorp.erp.modules.projects.dto.response.ProductivityResponse;
import com.vinncorp.erp.modules.projects.dto.response.TeamHeatmapResponse;
import com.vinncorp.erp.modules.projects.dto.response.ThroughputResponse;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.ProductivityAnalyticsService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductivityAnalyticsServiceImpl implements ProductivityAnalyticsService {

    private static final long CACHE_TTL = 300;
    private static final double DAYS_PER_WEEK = 7.0;

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final SprintCapacityRepository sprintCapacityRepository;
    private final SprintVelocitySnapshotRepository velocitySnapshotRepository;
    private final TimeLogRepository timeLogRepository;
    private final ProductivitySnapshotRepository productivitySnapshotRepository;
    private final CacheService cacheService;

    @Override
    public ProductivityResponse getProductivity(Long workspaceId, Long projectId) {
        String cacheKey = CacheNames.productivityAnalytics(projectId);
        Optional<ProductivityResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysAgo = now.minusDays(30);
        LocalDateTime thirtyDaysAgoDT = thirtyDaysAgo.atStartOfDay();

        List<Task> allTasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        List<Task> completedTasks = allTasks.stream()
                .filter(this::isCompleted)
                .collect(Collectors.toList());

        List<Task> recentCompleted = completedTasks.stream()
                .filter(t -> t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(thirtyDaysAgoDT))
                .toList();

        double throughput = recentCompleted.size() / 4.3;

        List<Sprint> completedSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.COMPLETED.name());

        double avgCycleTime = calculateAverageCycleTime(completedTasks, completedSprints);
        double avgLeadTime = calculateAverageLeadTime(completedTasks);
        double blockedTimeHours = calculateBlockedTimeHours(projectId, allTasks);
        double predictabilityScore = calculatePredictabilityScore(projectId, completedSprints);

        String trend = determineProductivityTrend(completedSprints);

        double productivityScore = calculateProductivityScore(throughput, avgCycleTime, predictabilityScore);

        ProductivityResponse response = ProductivityResponse.builder()
                .projectId(projectId)
                .throughput(Math.round(throughput * 100.0) / 100.0)
                .averageCycleTime(Math.round(avgCycleTime * 100.0) / 100.0)
                .averageLeadTime(Math.round(avgLeadTime * 100.0) / 100.0)
                .blockedTimeHours(Math.round(blockedTimeHours * 100.0) / 100.0)
                .predictabilityScore(Math.round(predictabilityScore * 100.0) / 100.0)
                .trend(trend)
                .productivityScore(Math.round(productivityScore * 100.0) / 100.0)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    @Override
    public ThroughputResponse getThroughput(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Task> allTasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        List<Task> completedTasks = allTasks.stream()
                .filter(t -> isCompleted(t) && t.getUpdatedAt() != null)
                .toList();

        LocalDate now = LocalDate.now();
        LocalDate eightWeeksAgo = now.minusWeeks(8);
        LocalDate startOfWeek = eightWeeksAgo.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (startOfWeek.isBefore(eightWeeksAgo)) {
            startOfWeek = startOfWeek.plusWeeks(1);
        }

        Map<String, Integer> weeklyCounts = new LinkedHashMap<>();
        int totalCompleted = 0;
        for (LocalDate weekStart = startOfWeek; !weekStart.isAfter(now); weekStart = weekStart.plusWeeks(1)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            LocalDateTime weekStartDT = weekStart.atStartOfDay();
            LocalDateTime weekEndDT = weekEnd.atTime(23, 59, 59);

            int count = (int) completedTasks.stream()
                    .filter(t -> t.getUpdatedAt() != null
                            && !t.getUpdatedAt().isBefore(weekStartDT)
                            && !t.getUpdatedAt().isAfter(weekEndDT))
                    .count();
            weeklyCounts.put(weekStart.toString(), count);
            totalCompleted += count;
        }

        int weeksWithData = weeklyCounts.size();
        double weeklyAverage = weeksWithData > 0 ? (double) totalCompleted / weeksWithData : 0;

        List<ThroughputResponse.ThroughputEntry> history = weeklyCounts.entrySet().stream()
                .map(e -> ThroughputResponse.ThroughputEntry.builder()
                        .period(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return ThroughputResponse.builder()
                .projectId(projectId)
                .totalCompleted(totalCompleted)
                .weeklyAverage(Math.round(weeklyAverage * 100.0) / 100.0)
                .history(history)
                .build();
    }

    @Override
    public CycleTimeResponse getCycleTime(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Sprint> completedSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.COMPLETED.name());

        List<Task> allTasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        List<Task> completedTasks = allTasks.stream()
                .filter(this::isCompleted)
                .collect(Collectors.toList());

        double avgCycleTime = calculateAverageCycleTime(completedTasks, completedSprints);
        double avgLeadTime = calculateAverageLeadTime(completedTasks);

        List<CycleTimeResponse.CycleTimeEntry> history = new ArrayList<>();
        for (Sprint sprint : completedSprints) {
            if (sprint.getStartDate() == null || sprint.getEndDate() == null) continue;
            List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprint.getId());
            List<Task> sprintCompleted = taskSprints.stream()
                    .map(TaskSprint::getTask)
                    .filter(t -> t != null && isCompleted(t))
                    .toList();

            double sprintCycleTime = 0;
            if (!sprintCompleted.isEmpty()) {
                sprintCycleTime = sprintCompleted.stream()
                        .filter(t -> t.getUpdatedAt() != null)
                        .mapToLong(t -> ChronoUnit.DAYS.between(sprint.getStartDate(), t.getUpdatedAt().toLocalDate()))
                        .average().orElse(0);
            }

            double sprintLeadTime = 0;
            if (!sprintCompleted.isEmpty()) {
                sprintLeadTime = sprintCompleted.stream()
                        .filter(t -> t.getCreatedAt() != null && t.getUpdatedAt() != null)
                        .mapToLong(t -> ChronoUnit.DAYS.between(t.getCreatedAt().toLocalDate(), t.getUpdatedAt().toLocalDate()))
                        .average().orElse(0);
            }

            history.add(CycleTimeResponse.CycleTimeEntry.builder()
                    .period(sprint.getName())
                    .cycleTime(Math.round(sprintCycleTime * 100.0) / 100.0)
                    .leadTime(Math.round(sprintLeadTime * 100.0) / 100.0)
                    .build());
        }

        String trend;
        if (history.size() >= 2) {
            double recent = history.getFirst().getCycleTime();
            double older = history.getLast().getCycleTime();
            if (recent < older * 0.9) trend = "IMPROVING";
            else if (recent > older * 1.1) trend = "DETERIORATING";
            else trend = "STABLE";
        } else {
            trend = "STABLE";
        }

        return CycleTimeResponse.builder()
                .averageCycleTime(Math.round(avgCycleTime * 100.0) / 100.0)
                .averageLeadTime(Math.round(avgLeadTime * 100.0) / 100.0)
                .trend(trend)
                .history(history)
                .build();
    }

    @Override
    public TeamHeatmapResponse getTeamHeatmap(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Task> allTasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        List<Sprint> activeSprints = sprintRepository
                .findByProjectIdAndStatusOrderByStartDateDesc(projectId, SprintStatus.ACTIVE.name());
        Set<Long> activeSprintTaskIds = new HashSet<>();
        for (Sprint sprint : activeSprints) {
            taskSprintRepository.findBySprintIdWithTasks(sprint.getId())
                    .stream().map(ts -> ts.getTask().getId())
                    .forEach(activeSprintTaskIds::add);
        }

        Map<Long, List<Task>> tasksByAssignee = allTasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId()));

        List<TeamHeatmapResponse.MemberProductivity> members = new ArrayList<>();

        for (Map.Entry<Long, List<Task>> entry : tasksByAssignee.entrySet()) {
            Long userId = entry.getKey();
            List<Task> userTasks = entry.getValue();

            User user = userTasks.getFirst().getAssignee();

            int completedTasks = (int) userTasks.stream().filter(this::isCompleted).count();
            int totalPoints = userTasks.stream()
                    .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                    .sum();

            double avgCycleTime = 0;
            List<Task> userCompleted = userTasks.stream()
                    .filter(t -> isCompleted(t) && t.getUpdatedAt() != null && t.getCreatedAt() != null)
                    .toList();
            if (!userCompleted.isEmpty()) {
                avgCycleTime = userCompleted.stream()
                        .mapToLong(t -> ChronoUnit.DAYS.between(t.getCreatedAt().toLocalDate(), t.getUpdatedAt().toLocalDate()))
                        .average().orElse(0);
            }

            double utilizationPercent = 0;
            for (Sprint sprint : activeSprints) {
                Optional<SprintCapacity> cap = sprintCapacityRepository
                        .findBySprintIdAndUserId(sprint.getId(), userId);
                if (cap.isPresent()) {
                    utilizationPercent = Math.max(utilizationPercent, cap.get().getUtilizationPercent());
                }
            }

            members.add(TeamHeatmapResponse.MemberProductivity.builder()
                    .userId(userId)
                    .userName(user != null ? user.getName() : "Unknown")
                    .completedTasks(completedTasks)
                    .totalPoints(totalPoints)
                    .averageCycleTime(Math.round(avgCycleTime * 100.0) / 100.0)
                    .utilizationPercent(Math.round(utilizationPercent * 100.0) / 100.0)
                    .build());
        }

        members.sort((a, b) -> Integer.compare(b.getCompletedTasks(), a.getCompletedTasks()));

        return TeamHeatmapResponse.builder()
                .projectId(projectId)
                .members(members)
                .build();
    }

    private boolean isCompleted(Task task) {
        if (task.getStatusEntity() == null) return false;
        String status = task.getStatusEntity().getName().toUpperCase().trim();
        return "DONE".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status);
    }

    private double calculateAverageCycleTime(List<Task> completedTasks, List<Sprint> completedSprints) {
        if (completedTasks.isEmpty() || completedSprints.isEmpty()) return 0;
        Map<Long, Sprint> sprintMap = completedSprints.stream()
                .collect(Collectors.toMap(Sprint::getId, s -> s));

        List<Double> cycleTimes = new ArrayList<>();
        for (Task task : completedTasks) {
            if (task.getUpdatedAt() == null) continue;
            Optional<TaskSprint> ts = taskSprintRepository.findByTaskId(task.getId());
            if (ts.isPresent()) {
                Sprint sprint = ts.get().getSprint();
                if (sprint != null && sprint.getStartDate() != null) {
                    long days = ChronoUnit.DAYS.between(
                            sprint.getStartDate(),
                            task.getUpdatedAt().toLocalDate());
                    cycleTimes.add((double) Math.max(days, 1));
                }
            }
        }

        return cycleTimes.isEmpty() ? 0 : cycleTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double calculateAverageLeadTime(List<Task> completedTasks) {
        List<Task> withDates = completedTasks.stream()
                .filter(t -> t.getCreatedAt() != null && t.getUpdatedAt() != null)
                .toList();
        if (withDates.isEmpty()) return 0;
        return withDates.stream()
                .mapToLong(t -> ChronoUnit.DAYS.between(
                        t.getCreatedAt().toLocalDate(),
                        t.getUpdatedAt().toLocalDate()))
                .average().orElse(0);
    }

    private double calculateBlockedTimeHours(Long projectId, List<Task> allTasks) {
        List<Long> taskIds = allTasks.stream().map(Task::getId).toList();
        if (taskIds.isEmpty()) return 0;
        List<Object[]> taskHours = timeLogRepository.getTaskHoursByProjectId(projectId);
        if (taskHours.isEmpty()) return 0;
        return taskHours.stream()
                .mapToDouble(row -> ((Number) row[1]).doubleValue())
                .sum() * 0.15;
    }

    private double calculatePredictabilityScore(Long projectId, List<Sprint> completedSprints) {
        List<SprintVelocitySnapshot> snapshots = velocitySnapshotRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.isEmpty()) return 1.0;
        double avgCompletionRate = snapshots.stream()
                .mapToDouble(SprintVelocitySnapshot::getCompletionRate)
                .average().orElse(0);
        return Math.min(avgCompletionRate, 1.0);
    }

    private String determineProductivityTrend(List<Sprint> completedSprints) {
        if (completedSprints.size() < 3) return "STABLE";
        List<Sprint> recent = completedSprints.stream().limit(3).toList();
        List<Sprint> older = completedSprints.stream()
                .skip(Math.max(0, completedSprints.size() - 3))
                .toList();

        double recentCompletion = recent.stream()
                .filter(s -> s.getSummaryCompletedTasks() != null && s.getSummaryTotalTasks() != null
                        && s.getSummaryTotalTasks() > 0)
                .mapToDouble(s -> (double) s.getSummaryCompletedTasks() / s.getSummaryTotalTasks())
                .average().orElse(0);
        double olderCompletion = older.stream()
                .filter(s -> s.getSummaryCompletedTasks() != null && s.getSummaryTotalTasks() != null
                        && s.getSummaryTotalTasks() > 0)
                .mapToDouble(s -> (double) s.getSummaryCompletedTasks() / s.getSummaryTotalTasks())
                .average().orElse(0);

        if (recentCompletion > olderCompletion * 1.1) return "IMPROVING";
        if (recentCompletion < olderCompletion * 0.9) return "DETERIORATING";
        return "STABLE";
    }

    private double calculateProductivityScore(double throughput, double avgCycleTime, double predictabilityScore) {
        double throughputScore = Math.min(throughput / 10.0, 1.0) * 40;
        double cycleTimeScore = Math.max(0, 1 - (avgCycleTime / 30.0)) * 30;
        double predictabilityWeighted = predictabilityScore * 30;
        return Math.min(throughputScore + cycleTimeScore + predictabilityWeighted, 100);
    }
}



