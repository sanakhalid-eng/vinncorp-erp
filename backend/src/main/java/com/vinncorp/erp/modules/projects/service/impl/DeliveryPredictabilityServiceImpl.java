package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.response.DeliveryPredictabilityResponse;
import com.vinncorp.erp.modules.projects.entity.DeliveryPredictabilitySnapshot;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import com.vinncorp.erp.modules.projects.repository.DeliveryPredictabilitySnapshotRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.DeliveryPredictabilityService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryPredictabilityServiceImpl implements DeliveryPredictabilityService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final DeliveryPredictabilitySnapshotRepository snapshotRepository;

    @Override
    @Transactional
    public DeliveryPredictabilityResponse analyze(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!workspaceId.equals(project.getWorkspace().getId())) {
            throw new ResourceNotFoundException("Project not found");
        }

        List<Task> tasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        LocalDateTime now = LocalDateTime.now();

        long withDue = tasks.stream().filter(t -> t.getDueDate() != null).count();
        long onTime = tasks.stream()
                .filter(t -> t.getDueDate() != null && isCompleted(t) && !t.getDueDate().isAfter(now))
                .count();
        double onTimeRate = withDue > 0 ? (double) onTime / withDue * 100 : 100;

        double avgDelay = tasks.stream()
                .filter(t -> t.getDueDate() != null && isCompleted(t) && t.getDueDate().isBefore(now))
                .mapToLong(t -> ChronoUnit.DAYS.between(t.getDueDate(), now))
                .average().orElse(0);

        double predictability = Math.max(0, Math.min(100, onTimeRate - avgDelay * 2));
        RiskLevel risk = predictability >= 80 ? RiskLevel.LOW
                : predictability >= 60 ? RiskLevel.MEDIUM
                : predictability >= 40 ? RiskLevel.HIGH : RiskLevel.CRITICAL;

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        DeliveryPredictabilitySnapshot snapshot = new DeliveryPredictabilitySnapshot();
        snapshot.setWorkspace(workspace);
        snapshot.setProjectId(projectId);
        snapshot.setPredictabilityScore(predictability);
        snapshot.setOnTimeDeliveryRate(onTimeRate);
        snapshot.setAvgDelayDays(avgDelay);
        snapshot.setRiskLevel(risk);
        snapshotRepository.save(snapshot);

        return DeliveryPredictabilityResponse.builder()
                .projectId(projectId)
                .predictabilityScore(predictability)
                .onTimeDeliveryRate(onTimeRate)
                .avgDelayDays(avgDelay)
                .riskLevel(risk)
                .build();
    }

    private boolean isCompleted(Task task) {
        if (task.getStatusEntity() == null) return false;
        String status = task.getStatusEntity().getName().toUpperCase().trim();
        return "DONE".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status);
    }
}



