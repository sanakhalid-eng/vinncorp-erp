package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.Task;

import java.util.List;

public interface DeadlineAutomationService {

    void autoShiftDueDates(Long taskId, int daysToShift);

    void rescheduleDependencyChain(Long taskId);

    List<Task> findOverdueTasksForEscalation(Long projectId);

    void adjustRecurringDeadline(Long taskId);

    void sprintAwareSchedule(Long taskId, Long sprintId);
}



