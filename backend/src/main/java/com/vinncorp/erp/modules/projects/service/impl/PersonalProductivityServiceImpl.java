package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.projects.dto.response.PersonalProductivityDashboardResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.PersonalProductivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalProductivityServiceImpl implements PersonalProductivityService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public PersonalProductivityDashboardResponse getDashboard(Long workspaceId, Long userId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        Set<Long> projectIds = projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(Project::getId).collect(Collectors.toSet());

        List<Task> tasks = taskRepository.findByAssignee_Id(userId).stream()
                .filter(t -> projectIds.contains(t.getProject().getId()))
                .toList();

        LocalDateTime weekStart = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusDays(7);
        LocalDateTime now = LocalDateTime.now();

        int completedThisWeek = (int) tasks.stream()
                .filter(t -> isCompleted(t) && t.getUpdatedAt() != null
                        && !t.getUpdatedAt().isBefore(weekStart) && t.getUpdatedAt().isBefore(weekEnd))
                .count();

        int dueThisWeek = (int) tasks.stream()
                .filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(weekStart)
                        && t.getDueDate().isBefore(weekEnd) && !isCompleted(t))
                .count();

        int overdue = (int) tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now) && !isCompleted(t))
                .count();

        double focusScore = tasks.isEmpty() ? 100
                : Math.max(0, 100 - (overdue * 10.0) - (dueThisWeek * 2.0));

        List<String> topPriorities = tasks.stream()
                .filter(t -> !isCompleted(t))
                .sorted(Comparator
                        .comparing((Task t) -> t.getDueDate() == null ? LocalDateTime.MAX : t.getDueDate())
                        .thenComparing(t -> t.getPriority() != null ? t.getPriority().ordinal() : 99))
                .limit(5)
                .map(Task::getTitle)
                .toList();

        return PersonalProductivityDashboardResponse.builder()
                .tasksCompletedThisWeek(completedThisWeek)
                .tasksDueThisWeek(dueThisWeek)
                .overdueTasks(overdue)
                .focusScore(focusScore)
                .topPriorities(topPriorities)
                .build();
    }

    private boolean isCompleted(Task task) {
        if (task.getStatusEntity() == null) return false;
        String status = task.getStatusEntity().getName().toUpperCase().trim();
        return "DONE".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status);
    }
}



