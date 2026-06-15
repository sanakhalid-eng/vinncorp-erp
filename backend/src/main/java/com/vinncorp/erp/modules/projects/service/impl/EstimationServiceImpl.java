package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.enums.EstimationConfidence;
import com.vinncorp.erp.modules.projects.dto.response.EstimationAccuracyResponse;
import com.vinncorp.erp.modules.projects.dto.response.EstimationResponse;
import com.vinncorp.erp.modules.projects.dto.response.VelocityPredictionResponse;
import com.vinncorp.erp.modules.projects.engine.TaskComplexityAnalyzer;
import com.vinncorp.erp.modules.projects.entity.EstimationSnapshot;
import com.vinncorp.erp.modules.projects.entity.SprintVelocitySnapshot;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.EstimationSnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.SprintVelocitySnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.EstimationService;
import com.vinncorp.erp.modules.projects.service.VelocityPredictionService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.modules.projects.dto.response.EstimationResponse.HistoricalComparison;
import com.vinncorp.erp.modules.projects.dto.response.EstimationResponse.PredictedCompletion;
import com.vinncorp.erp.modules.projects.dto.response.EstimationResponse.SimilarTask;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EstimationServiceImpl implements EstimationService {

    private static final long CACHE_TTL = 600_000;

    private final TaskRepository taskRepository;
    private final EstimationSnapshotRepository estimationSnapshotRepository;
    private final TaskComplexityAnalyzer complexityAnalyzer;
    private final VelocityPredictionService velocityPredictionService;
    private final SprintVelocitySnapshotRepository sprintVelocitySnapshotRepository;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;

    @Override
    public EstimationResponse getTaskEstimate(Long workspaceId, Long taskId) {
        String cacheKey = CacheNames.estimation(workspaceId, taskId);
        Optional<EstimationResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        int recommendedPoints = complexityAnalyzer.analyze(task);
        double confidence = complexityAnalyzer.calculateConfidence(task);
        List<String> reasoningFactors = complexityAnalyzer.getReasoningFactors(task);

        HistoricalComparison historicalComparison = buildHistoricalComparison(workspaceId, task);
        PredictedCompletion predictedCompletion = buildPredictedCompletion(recommendedPoints);

        EstimationResponse response = EstimationResponse.builder()
                .recommendedPoints(recommendedPoints)
                .confidence(mapConfidence(confidence))
                .reasoningFactors(reasoningFactors)
                .historicalComparison(historicalComparison)
                .predictedCompletion(predictedCompletion)
                .build();

        cacheService.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    @Override
    public EstimationAccuracyResponse getProjectEstimationAccuracy(Long workspaceId, Long projectId) {
        List<EstimationSnapshot> snapshots = estimationSnapshotRepository
                .findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(workspaceId, projectId);

        if (snapshots.isEmpty()) {
            return EstimationAccuracyResponse.builder()
                    .totalTasks(0)
                    .onTarget(0)
                    .overEstimated(0)
                    .underEstimated(0)
                    .averageDrift(0.0)
                    .accuracyPercent(100.0)
                    .build();
        }

        int onTarget = 0;
        int overEstimated = 0;
        int underEstimated = 0;
        double totalDrift = 0;

        for (EstimationSnapshot snapshot : snapshots) {
            Double drift = snapshot.getEstimationDrift();
            if (drift == null) continue;

            totalDrift += Math.abs(drift);

            if (Math.abs(drift) <= 0.1) {
                onTarget++;
            } else if (drift > 0) {
                overEstimated++;
            } else {
                underEstimated++;
            }
        }

        int totalWithDrift = onTarget + overEstimated + underEstimated;
        double averageDrift = totalWithDrift > 0 ? totalDrift / totalWithDrift : 0;
        double accuracyPercent = totalWithDrift > 0
                ? ((double) onTarget / totalWithDrift) * 100
                : 100.0;

        return EstimationAccuracyResponse.builder()
                .totalTasks(totalWithDrift)
                .onTarget(onTarget)
                .overEstimated(overEstimated)
                .underEstimated(underEstimated)
                .averageDrift(averageDrift)
                .accuracyPercent(accuracyPercent)
                .build();
    }

    @Override
    public VelocityPredictionResponse getVelocityPrediction(Long workspaceId, Long projectId) {
        List<SprintVelocitySnapshot> snapshots =
                sprintVelocitySnapshotRepository.findTop5ByProjectIdOrderByCreatedAtDesc(projectId);

        double currentVelocity = snapshots.isEmpty() ? 0 : snapshots.get(0).getVelocityScore();
        double predictedVelocity = velocityPredictionService.predictVelocity(projectId);
        double confidence = velocityPredictionService.calculateVelocityConfidence(projectId);

        String trend;
        if (snapshots.size() < 2) {
            trend = "STABLE";
        } else {
            double recent = snapshots.get(0).getVelocityScore();
            double previous = snapshots.get(snapshots.size() - 1).getVelocityScore();
            double diff = recent - previous;
            if (diff > 5) trend = "INCREASING";
            else if (diff < -5) trend = "DECREASING";
            else trend = "STABLE";
        }

        List<VelocityPredictionResponse.SprintVelocity> sprintVelocities = snapshots.stream()
                .map(s -> VelocityPredictionResponse.SprintVelocity.builder()
                        .sprintId(s.getSprintId())
                        .sprintName("Sprint " + s.getSprintId())
                        .committedPoints(s.getCommittedPoints())
                        .completedPoints(s.getCompletedPoints())
                        .completionRate(s.getCompletionRate())
                        .build())
                .collect(Collectors.toList());

        return VelocityPredictionResponse.builder()
                .currentVelocity(currentVelocity)
                .predictedVelocity(predictedVelocity)
                .sprintCount(snapshots.size())
                .trend(trend)
                .confidencePercent(confidence * 100)
                .sprintVelocities(sprintVelocities)
                .build();
    }

    @Override
    public EstimationResponse getSimilarEstimates(Long workspaceId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        List<Task> projectTasks = taskRepository.findByProjectId(task.getProject().getId(), Pageable.unpaged()).getContent();

        List<SimilarTask> similarTasks = findSimilarTasks(task, projectTasks);

        double avgPoints = similarTasks.stream()
                .filter(st -> st.getStoryPoints() > 0)
                .mapToInt(SimilarTask::getStoryPoints)
                .average()
                .orElse(3.0);

        int recommendedPoints = (int) Math.round(avgPoints);
        double confidence = similarTasks.isEmpty() ? 0.3
                : Math.min(0.95, similarTasks.size() * 0.15);

        List<EstimationSnapshot> snapshots = estimationSnapshotRepository
                .findByWorkspaceIdAndProjectIdAndDeletedAtIsNull(workspaceId, task.getProject().getId());

        HistoricalComparison historicalComparison = HistoricalComparison.builder()
                .previousEstimatesCount(snapshots.size())
                .averagePreviousPoints(snapshots.stream()
                        .filter(s -> s.getEstimatedPoints() != null)
                        .mapToInt(EstimationSnapshot::getEstimatedPoints)
                        .average().orElse(0))
                .accuracyRate(snapshots.stream()
                        .filter(s -> s.getEstimationDrift() != null)
                        .mapToDouble(EstimationSnapshot::getEstimationDrift)
                        .map(d -> Math.max(0, 1 - Math.abs(d)))
                        .average().orElse(0))
                .similarTasks(similarTasks)
                .build();

        PredictedCompletion predictedCompletion = PredictedCompletion.builder()
                .estimatedDays(Math.max(1, recommendedPoints * 2))
                .confidencePercent(confidence * 100)
                .riskLevel(recommendedPoints > 13 ? "HIGH" : recommendedPoints > 8 ? "MEDIUM" : "LOW")
                .build();

        EstimationResponse response = EstimationResponse.builder()
                .recommendedPoints(recommendedPoints)
                .confidence(mapConfidence(confidence))
                .reasoningFactors(List.of("Based on " + similarTasks.size() + " similar tasks"))
                .historicalComparison(historicalComparison)
                .predictedCompletion(predictedCompletion)
                .build();

        return response;
    }

    private HistoricalComparison buildHistoricalComparison(Long workspaceId, Task task) {
        List<EstimationSnapshot> snapshots = estimationSnapshotRepository
                .findByWorkspaceIdAndProjectIdAndTaskIdAndDeletedAtIsNull(
                        workspaceId, task.getProject().getId(), task.getId());

        if (snapshots.isEmpty()) {
            return HistoricalComparison.builder()
                    .previousEstimatesCount(0)
                    .averagePreviousPoints(0)
                    .accuracyRate(0)
                    .similarTasks(Collections.emptyList())
                    .build();
        }

        double avgPoints = snapshots.stream()
                .filter(s -> s.getEstimatedPoints() != null)
                .mapToInt(EstimationSnapshot::getEstimatedPoints)
                .average().orElse(0);

        double accuracyRate = snapshots.stream()
                .filter(s -> s.getEstimationDrift() != null)
                .mapToDouble(EstimationSnapshot::getEstimationDrift)
                .map(d -> Math.max(0, 1 - Math.abs(d)))
                .average().orElse(0);

        double avgDrift = snapshots.stream()
                .filter(s -> s.getEstimationDrift() != null)
                .mapToDouble(EstimationSnapshot::getEstimationDrift)
                .average().orElse(0);

        if (Math.abs(avgDrift) > 0.3) {
            eventPublisher.publish(DomainEvent.builder()
                    .type(DomainEvent.Type.ESTIMATION_DRIFT_DETECTED)
                    .actorId(task.getCreatedBy())
                    .entityType("Task")
                    .entityId(task.getId())
                    .projectId(task.getProject().getId())
                    .message("Estimation drift of " + String.format("%.2f", avgDrift) + " detected for task: " + task.getTitle())
                    .build());
        }

        return HistoricalComparison.builder()
                .previousEstimatesCount(snapshots.size())
                .averagePreviousPoints(avgPoints)
                .accuracyRate(accuracyRate)
                .similarTasks(Collections.emptyList())
                .build();
    }

    private PredictedCompletion buildPredictedCompletion(int storyPoints) {
        int estimatedDays = Math.max(1, storyPoints * 2);
        double confidencePercent = storyPoints <= 3 ? 90 : storyPoints <= 8 ? 75 : 60;
        String riskLevel = storyPoints > 13 ? "HIGH" : storyPoints > 8 ? "MEDIUM" : "LOW";

        return PredictedCompletion.builder()
                .estimatedDays(estimatedDays)
                .confidencePercent(confidencePercent)
                .riskLevel(riskLevel)
                .build();
    }

    private List<SimilarTask> findSimilarTasks(Task sourceTask, List<Task> candidateTasks) {
        List<SimilarTask> result = new ArrayList<>();
        String sourceTitle = sourceTask.getTitle() != null ? sourceTask.getTitle() : "";

        for (Task candidate : candidateTasks) {
            if (candidate.getId().equals(sourceTask.getId())) continue;

            String candidateTitle = candidate.getTitle() != null ? candidate.getTitle() : "";
            double similarity = calculateTitleSimilarity(sourceTitle, candidateTitle);

            if (similarity > 0.2) {
                result.add(SimilarTask.builder()
                        .taskId(candidate.getId())
                        .title(candidateTitle)
                        .storyPoints(0)
                        .similarityScore(similarity)
                        .build());
            }
        }

        result.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        return result.stream().limit(5).collect(Collectors.toList());
    }

    private double calculateTitleSimilarity(String title1, String title2) {
        if (title1 == null || title2 == null) return 0;
        if (title1.isEmpty() && title2.isEmpty()) return 1;
        if (title1.isEmpty() || title2.isEmpty()) return 0;

        String t1 = title1.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String t2 = title2.toLowerCase().replaceAll("[^a-z0-9\\s]", "");

        Set<String> words1 = new HashSet<>();
        Set<String> words2 = new HashSet<>();
        Collections.addAll(words1, t1.split("\\s+"));
        Collections.addAll(words2, t2.split("\\s+"));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        if (union.isEmpty()) return 0;

        if (words1.equals(words2)) return 1.0;

        return (double) intersection.size() / union.size();
    }

    private EstimationConfidence mapConfidence(double score) {
        if (score >= 0.9) return EstimationConfidence.VERY_HIGH;
        if (score >= 0.7) return EstimationConfidence.HIGH;
        if (score >= 0.5) return EstimationConfidence.MEDIUM;
        if (score >= 0.3) return EstimationConfidence.LOW;
        return EstimationConfidence.VERY_LOW;
    }
}



