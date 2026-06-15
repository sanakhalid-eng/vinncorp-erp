package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.CalendarIntelligenceResponse;
import com.vinncorp.erp.modules.projects.dto.response.CalendarResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskCalendarResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.CalendarIntelligenceService;
import com.vinncorp.erp.modules.projects.service.CalendarService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarIntelligenceServiceImpl implements CalendarIntelligenceService {

    private final CalendarService calendarService;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public CalendarIntelligenceResponse analyze(Long workspaceId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!workspaceId.equals(project.getWorkspace().getId())) {
            throw new ResourceNotFoundException("Project not found");
        }

        CalendarResponse calendar = calendarService.getCalendarData(projectId);
        List<TaskCalendarResponse> tasks = calendar.getTasks() != null ? calendar.getTasks() : List.of();

        Map<LocalDate, Integer> loadByDay = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(14);

        for (TaskCalendarResponse task : tasks) {
            if (task.getDueDate() == null) continue;
            LocalDate due = LocalDate.parse(task.getDueDate());
            if (!due.isBefore(today) && !due.isAfter(horizon)) {
                loadByDay.merge(due, 1, Integer::sum);
            }
        }

        int overloadDays = (int) loadByDay.values().stream().filter(count -> count >= 4).count();
        int focusBlocks = Math.max(1, overloadDays / 2);

        List<String> warnings = new ArrayList<>();
        loadByDay.entrySet().stream()
                .filter(e -> e.getValue() >= 4)
                .forEach(e -> warnings.add("Heavy load on " + e.getKey() + " (" + e.getValue() + " tasks)"));
        if (calendar.getOverdueTasksCount() > 0) {
            warnings.add(calendar.getOverdueTasksCount() + " overdue tasks need attention");
        }

        List<TaskCalendarResponse> critical = tasks.stream()
                .filter(t -> {
                    if (t.getDueDate() == null) return false;
                    LocalDate due = LocalDate.parse(t.getDueDate());
                    return !due.isBefore(today) && !due.isAfter(today.plusDays(3));
                })
                .limit(10)
                .toList();

        return CalendarIntelligenceResponse.builder()
                .overloadDays(overloadDays)
                .focusBlocksRecommended(focusBlocks)
                .conflictWarnings(warnings)
                .upcomingCritical(critical)
                .build();
    }
}



