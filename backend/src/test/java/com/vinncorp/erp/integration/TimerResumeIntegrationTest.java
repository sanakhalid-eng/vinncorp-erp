package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;

import com.vinncorp.erp.modules.projects.dto.request.TimeLogRequest;
import com.vinncorp.erp.modules.projects.entity.ActiveTimer;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TimeLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TimerResumeIntegrationTest extends AbstractIntegrationTest {

    private Task testTask;

    @BeforeEach
    void setUp() {
        activeTimerRepository.deleteAll();
        timeLogRepository.deleteAll();
        testTask = createTask("Timer Test Task", normalUser, defaultStatus);
    }

    @Test
    void startTimer_shouldCreateActiveTimer() {
        ActiveTimer timer = timeTrackingService.startTimer(testTask.getId(), normalUser.getId(), "Working on task");

        assertNotNull(timer.getId());
        assertEquals(normalUser.getId(), timer.getUser().getId());
        assertEquals(testTask.getId(), timer.getTask().getId());
        assertNotNull(timer.getStartedAt());
    }

    @Test
    void startTimer_whenAlreadyRunning_shouldFail() {
        timeTrackingService.startTimer(testTask.getId(), normalUser.getId(), "First session");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                timeTrackingService.startTimer(testTask.getId(), normalUser.getId(), "Second session"));
        assertTrue(ex.getMessage().contains("already has an active timer"));
    }

    @Test
    void stopTimer_shouldCreateTimeLogAndDeleteTimer() throws InterruptedException {
        timeTrackingService.startTimer(testTask.getId(), normalUser.getId(), "Testing timer");
        Thread.sleep(1100);

        TimeLog timeLog = timeTrackingService.stopTimer(normalUser.getId(), "Completed session");

        assertNotNull(timeLog.getId());
        assertEquals(testTask.getId(), timeLog.getTask().getId());
        assertTrue(timeLog.getHours().compareTo(java.math.BigDecimal.ZERO) > 0);

        Optional<ActiveTimer> remaining = timeTrackingService.getActiveTimer(normalUser.getId());
        assertTrue(remaining.isEmpty());
    }

    @Test
    void stopTimer_whenNotRunning_shouldFail() {
        assertThrows(IllegalStateException.class, () ->
                timeTrackingService.stopTimer(normalUser.getId(), "No timer"));
    }

    @Test
    void getActiveTimer_shouldReturnCurrentTimer() {
        timeTrackingService.startTimer(testTask.getId(), normalUser.getId(), "Active session");

        Optional<ActiveTimer> timer = timeTrackingService.getActiveTimer(normalUser.getId());

        assertTrue(timer.isPresent());
        assertEquals(normalUser.getId(), timer.get().getUser().getId());
    }

    @Test
    void getActiveTimer_whenNoTimer_shouldReturnEmpty() {
        Optional<ActiveTimer> timer = timeTrackingService.getActiveTimer(normalUser.getId());
        assertTrue(timer.isEmpty());
    }

    @Test
    void hasActiveTimer_shouldReturnCorrectStatus() throws InterruptedException {
        assertFalse(timeTrackingService.hasActiveTimer(normalUser.getId()));

        timeTrackingService.startTimer(testTask.getId(), normalUser.getId(), "Test");
        assertTrue(timeTrackingService.hasActiveTimer(normalUser.getId()));

        Thread.sleep(1100);
        timeTrackingService.stopTimer(normalUser.getId(), "Done");
        assertFalse(timeTrackingService.hasActiveTimer(normalUser.getId()));
    }

    @Test
    void startTimerOnDifferentTask_whileRunning_shouldFail() {
        timeTrackingService.startTimer(testTask.getId(), normalUser.getId(), "First task");

        Task otherTask = createTask("Other Task", normalUser, defaultStatus);
        assertThrows(IllegalStateException.class, () ->
                timeTrackingService.startTimer(otherTask.getId(), normalUser.getId(), "Other task"));
    }

    @Test
    void logTime_manually_shouldCreateTimeLog() {
        var request = new TimeLogRequest();
        request.setHours(java.math.BigDecimal.valueOf(3.5));
        request.setDescription("Manual log");
        request.setLogDate(java.time.LocalDate.now());

        TimeLog timeLog = timeTrackingService.logTime(testTask.getId(), normalUser.getId(), request);

        assertNotNull(timeLog.getId());
        assertEquals(0, java.math.BigDecimal.valueOf(3.5).compareTo(timeLog.getHours()));
    }

    @Test
    void logTime_withInvalidHours_shouldFail() {
        var request = new TimeLogRequest();
        request.setHours(java.math.BigDecimal.valueOf(-1));

        assertThrows(IllegalArgumentException.class, () ->
                timeTrackingService.logTime(testTask.getId(), normalUser.getId(), request));
    }

    @Test
    void logTime_exceedingDailyLimit_shouldFail() {
        var request = new TimeLogRequest();
        request.setHours(java.math.BigDecimal.valueOf(25));

        assertThrows(IllegalArgumentException.class, () ->
                timeTrackingService.logTime(testTask.getId(), normalUser.getId(), request));
    }
}

