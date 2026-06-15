package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.repository.TaskDependencyRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskComplexityAnalyzer {
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;

    public int analyze(Task task) {
        int basePoints = 3;
        String title = task.getTitle() != null ? task.getTitle() : "";
        String description = task.getDescription() != null ? task.getDescription() : "";

        if (title.length() > 80) basePoints += 1;
        if (description.length() > 500) basePoints += 2;
        if (description.length() > 2000) basePoints += 1;

        int depCount = dependencyRepository.findByTaskId(task.getId()).size();
        basePoints += Math.min(depCount, 3);

        int subtaskCount = (int) taskRepository.countByParentTaskId(task.getId());
        basePoints += Math.min(subtaskCount, 2);

        if (task.getPriority() != null) {
            switch (task.getPriority()) {
                case CRITICAL -> basePoints += 4;
                case HIGH -> basePoints += 3;
                case MEDIUM -> basePoints += 2;
                case LOW -> basePoints -= 1;
            }
        }

        return Math.max(1, basePoints);
    }

    public double calculateConfidence(Task task) {
        int factors = 0;
        if (task.getTitle() != null && !task.getTitle().isEmpty()) factors++;
        if (task.getDescription() != null && task.getDescription().length() > 50) factors++;
        if (!dependencyRepository.findByTaskId(task.getId()).isEmpty()) factors++;
        if (taskRepository.countByParentTaskId(task.getId()) > 0) factors++;
        if (task.getPriority() != null) factors++;
        if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) factors++;
        return Math.min(1.0, factors * 0.2);
    }

    public List<String> getReasoningFactors(Task task) {
        List<String> factors = new java.util.ArrayList<>();
        if (task.getTitle() != null && task.getTitle().length() > 80)
            factors.add("Long title (+1 point)");
        if (task.getDescription() != null && task.getDescription().length() > 500)
            factors.add("Detailed description (+2 points)");
        int depCount = dependencyRepository.findByTaskId(task.getId()).size();
        if (depCount > 0) factors.add(depCount + " dependencies (+" + Math.min(depCount, 3) + " points)");
        int subtaskCount = (int) taskRepository.countByParentTaskId(task.getId());
        if (subtaskCount > 0) factors.add(subtaskCount + " subtasks (+" + Math.min(subtaskCount, 2) + " points)");
        return factors;
    }
}



