package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.modules.projects.dto.response.SprintResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SprintCompletionIntegrationTest extends AbstractIntegrationTest {

    private Sprint activeSprint;

    @BeforeEach
    void setUp() {
        taskSprintRepository.deleteAll();
        sprintRepository.deleteAll();
        taskRepository.deleteAll();

        activeSprint = createSprint("Sprint 1", SprintStatus.ACTIVE);
    }

    @Test
    void completeActiveSprint_shouldSucceed() {
        SprintResponse response = sprintService.completeSprint(activeSprint.getId(), adminUser.getEmail(), false);

        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void completeNonActiveSprint_shouldFail() {
        Sprint plannedSprint = createSprint("Planned Sprint", SprintStatus.PLANNED);

        assertThrows(BadRequestException.class, () ->
                sprintService.completeSprint(plannedSprint.getId(), adminUser.getEmail(), false));
    }

    @Test
    void completeCompletedSprint_shouldFail() {
        SprintResponse completed = sprintService.completeSprint(activeSprint.getId(), adminUser.getEmail(), false);
        assertEquals("COMPLETED", completed.getStatus());

        assertThrows(BadRequestException.class, () ->
                sprintService.completeSprint(activeSprint.getId(), adminUser.getEmail(), false));
    }

    @Test
    void completeSprint_withTasks_shouldTrackStats() {
        Task task1 = createTask("Task 1", normalUser, defaultStatus);
        Task task2 = createTask("Task 2", normalUser, doneStatus);

        TaskSprint ts1 = new TaskSprint();
        ts1.setTask(task1);
        ts1.setSprint(activeSprint);
        taskSprintRepository.save(ts1);

        TaskSprint ts2 = new TaskSprint();
        ts2.setTask(task2);
        ts2.setSprint(activeSprint);
        taskSprintRepository.save(ts2);

        SprintResponse response = sprintService.completeSprint(activeSprint.getId(), adminUser.getEmail(), false);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(2, response.getSummaryTotalTasks().intValue());
        assertEquals(1, response.getSummaryCompletedTasks().intValue());
        assertEquals(0, response.getSummaryCarriedForward().intValue());
    }

    @Test
    void completeSprint_withCarryForward_shouldMoveUnfinishedTasks() {
        Task task1 = createTask("Done Task", normalUser, doneStatus);
        Task task2 = createTask("Unfinished Task", normalUser, defaultStatus);

        TaskSprint ts1 = new TaskSprint();
        ts1.setTask(task1);
        ts1.setSprint(activeSprint);
        taskSprintRepository.save(ts1);

        TaskSprint ts2 = new TaskSprint();
        ts2.setTask(task2);
        ts2.setSprint(activeSprint);
        taskSprintRepository.save(ts2);

        SprintResponse response = sprintService.completeSprint(activeSprint.getId(), adminUser.getEmail(), true);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(2, response.getSummaryTotalTasks().intValue());
        assertEquals(1, response.getSummaryCompletedTasks().intValue());
        assertEquals(1, response.getSummaryCarriedForward().intValue());
    }

    @Test
    void completeSprint_withoutCarryForward_shouldNotMoveUnfinishedTasks() {
        Task task = createTask("Unfinished", normalUser, defaultStatus);

        TaskSprint ts = new TaskSprint();
        ts.setTask(task);
        ts.setSprint(activeSprint);
        taskSprintRepository.save(ts);

        SprintResponse response = sprintService.completeSprint(activeSprint.getId(), adminUser.getEmail(), false);

        assertEquals(1, response.getSummaryTotalTasks().intValue());
        assertEquals(1, response.getSummaryCompletedTasks().intValue());
        assertEquals(0, response.getSummaryCarriedForward().intValue());
    }

    @Test
    void completeSprint_withEmptySprint_shouldSucceed() {
        SprintResponse response = sprintService.completeSprint(activeSprint.getId(), adminUser.getEmail(), false);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(0, response.getSummaryTotalTasks().intValue());
        assertEquals(0, response.getSummaryCompletedTasks().intValue());
    }

    @Test
    void completeSprint_withNonexistentSprint_shouldFail() {
        assertThrows(Exception.class, () ->
                sprintService.completeSprint(99999L, adminUser.getEmail(), false));
    }
}

