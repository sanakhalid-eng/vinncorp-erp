package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.SprintVelocitySnapshot;
import com.vinncorp.erp.modules.projects.repository.SprintVelocitySnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VelocityPredictionService {
    private final SprintVelocitySnapshotRepository velocityRepo;

    public double predictVelocity(Long projectId) {
        var snapshots = velocityRepo.findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.isEmpty()) return 10.0;
        double total = snapshots.stream().mapToDouble(s -> (double) s.getCompletedPoints()).sum();
        double avg = total / snapshots.size();
        if (snapshots.size() < 3) return avg;
        double[] rates = snapshots.stream().mapToDouble(SprintVelocitySnapshot::getCompletionRate).toArray();
        double trend = (rates[0] - rates[rates.length - 1]) / rates.length;
        return Math.max(1, avg * (1 + trend * 0.1));
    }

    public double calculateVelocityConfidence(Long projectId) {
        var snapshots = velocityRepo.findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.size() < 2) return 0.3;
        double variance = 0;
        double mean = snapshots.stream().mapToInt(SprintVelocitySnapshot::getCompletedPoints).average().orElse(0);
        for (var s : snapshots) {
            variance += Math.pow(s.getCompletedPoints() - mean, 2);
        }
        variance /= snapshots.size();
        double cv = Math.sqrt(variance) / (mean == 0 ? 1 : mean);
        return Math.max(0.1, Math.min(0.95, 1.0 - cv));
    }
}



