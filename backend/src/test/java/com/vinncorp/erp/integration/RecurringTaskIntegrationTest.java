package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;

import com.vinncorp.erp.modules.projects.dto.request.CreateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.request.UpdateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.response.RecurringOccurrenceResponse;
import com.vinncorp.erp.modules.projects.dto.response.RecurringTemplateResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.enums.RecurrenceType;
import com.vinncorp.erp.modules.projects.service.RecurringTaskService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecurringTaskIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RecurringTaskService recurringTaskService;

    private Task templateTask;
    private Task dependencyTask;

    @BeforeEach
    void setUp() {
        taskDependencyRepository.deleteAll();
        taskRepository.deleteAll();

        templateTask = createTask("Daily Standup", normalUser, defaultStatus);
        dependencyTask = createTask("Prep Work", normalUser, defaultStatus);
    }

    @Test
    void createDailyRecurring_shouldSucceed() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        RecurringTemplateResponse response = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(RecurrenceType.DAILY, response.getRecurrenceType());
        assertEquals(1, response.getIntervalValue());
        assertTrue(response.isActive());
        assertFalse(response.isPaused());
        assertEquals(templateTask.getId(), response.getTemplateTaskId());
    }

    @Test
    void createWeeklyRecurring_shouldSucceed() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.WEEKLY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setDaysOfWeek(List.of("MONDAY", "WEDNESDAY", "FRIDAY"));

        RecurringTemplateResponse response = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        assertNotNull(response);
        assertEquals(RecurrenceType.WEEKLY, response.getRecurrenceType());
        assertTrue(response.getDaysOfWeek().contains("MONDAY"));
        assertTrue(response.getDaysOfWeek().contains("WEDNESDAY"));
        assertTrue(response.getDaysOfWeek().contains("FRIDAY"));
    }

    @Test
    void createMonthlyRecurring_shouldSucceed() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.MONTHLY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setDayOfMonth(15);

        RecurringTemplateResponse response = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        assertNotNull(response);
        assertEquals(RecurrenceType.MONTHLY, response.getRecurrenceType());
        assertEquals(15, response.getDayOfMonth());
    }

    @Test
    void createDuplicateRecurring_shouldFail() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail());

        assertThrows(BadRequestException.class, () ->
                recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail()));
    }

    @Test
    void pauseResumeRecurring_shouldWork() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        RecurringTemplateResponse paused = recurringTaskService.pauseRecurring(created.getId(), adminUser.getEmail());
        assertTrue(paused.isPaused());

        RecurringTemplateResponse resumed = recurringTaskService.resumeRecurring(created.getId(), adminUser.getEmail());
        assertFalse(resumed.isPaused());
    }

    @Test
    void stopRecurring_shouldDeactivate() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.stopRecurring(created.getId(), adminUser.getEmail());

        RecurringTemplateResponse stopped = recurringTaskService.getRecurringTemplate(created.getId());
        assertFalse(stopped.isActive());
    }

    @Test
    void updateRecurring_shouldModifyConfig() {
        CreateRecurringRequest createReq = new CreateRecurringRequest();
        createReq.setRecurrenceType(RecurrenceType.DAILY);
        createReq.setIntervalValue(1);
        createReq.setStartDate(LocalDateTime.now().plusDays(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), createReq, adminUser.getEmail());

        UpdateRecurringRequest updateReq = new UpdateRecurringRequest();
        updateReq.setRecurrenceType(RecurrenceType.WEEKLY);
        updateReq.setIntervalValue(2);
        updateReq.setDaysOfWeek(List.of("MONDAY", "FRIDAY"));

        RecurringTemplateResponse updated = recurringTaskService.updateRecurring(created.getId(), updateReq, adminUser.getEmail());

        assertEquals(RecurrenceType.WEEKLY, updated.getRecurrenceType());
        assertEquals(2, updated.getIntervalValue());
        assertTrue(updated.getDaysOfWeek().contains("MONDAY"));
        assertTrue(updated.getDaysOfWeek().contains("FRIDAY"));
    }

    @Test
    void weeklyRecurring_withoutDays_shouldFail() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.WEEKLY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        assertThrows(BadRequestException.class, () ->
                recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail()));
    }

    @Test
    void monthlyRecurring_withoutDayOfMonth_shouldFail() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.MONTHLY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        assertThrows(BadRequestException.class, () ->
                recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail()));
    }

    @Test
    void getTemplatesByProject_shouldReturnTemplates() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail());

        List<RecurringTemplateResponse> templates = recurringTaskService.getTemplatesByProject(testProject.getId());
        assertFalse(templates.isEmpty());
        assertEquals(1, templates.size());
    }

    @Test
    void generateOccurrences_shouldCreateTask() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().minusMinutes(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.generateNextOccurrences();

        List<RecurringOccurrenceResponse> occurrences = recurringTaskService.getOccurrences(created.getId());
        assertFalse(occurrences.isEmpty());
        assertEquals(1, occurrences.size());
        assertEquals("GENERATED", occurrences.get(0).getGenerationStatus());
        assertNotNull(occurrences.get(0).getGeneratedTaskId());
    }

    @Test
    void generateOccurrences_duplicatePrevention() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().minusMinutes(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.generateNextOccurrences();
        recurringTaskService.generateNextOccurrences();

        List<RecurringOccurrenceResponse> occurrences = recurringTaskService.getOccurrences(created.getId());
        assertEquals(1, occurrences.size());
    }

    @Test
    void generateOccurrences_withMaxOccurrences_shouldStop() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().minusMinutes(1));
        request.setMaxOccurrences(2);

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.generateNextOccurrences();

        List<RecurringOccurrenceResponse> occurrences = recurringTaskService.getOccurrences(created.getId());
        assertEquals(1, occurrences.size());
    }

    @Test
    void pausedRecurring_shouldNotGenerate() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().minusMinutes(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.pauseRecurring(created.getId(), adminUser.getEmail());
        recurringTaskService.generateNextOccurrences();

        List<RecurringOccurrenceResponse> occurrences = recurringTaskService.getOccurrences(created.getId());
        assertTrue(occurrences.isEmpty());
    }

    @Test
    void workspaceIsolation_shouldSeparateTemplates() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail());

        List<RecurringTemplateResponse> templates = recurringTaskService.getTemplatesByProject(testProject.getId());
        assertEquals(1, templates.size());
    }

    @Test
    void occurrenceHistory_shouldShowAllGenerations() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().minusMinutes(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.generateNextOccurrences();

        List<RecurringOccurrenceResponse> occurrences = recurringTaskService.getOccurrences(created.getId());
        assertEquals(1, occurrences.size());
        assertEquals(LocalDate.now(), occurrences.get(0).getOccurrenceDate());
    }

    @Test
    void softDeleteTemplate_shouldNotAffectGeneratedTasks() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().minusMinutes(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.generateNextOccurrences();

        List<RecurringOccurrenceResponse> occurrences = recurringTaskService.getOccurrences(created.getId());
        assertEquals(1, occurrences.size());
        Long generatedTaskId = occurrences.get(0).getGeneratedTaskId();

        taskService.deleteTask(generatedTaskId);

        List<RecurringOccurrenceResponse> afterDelete = recurringTaskService.getOccurrences(created.getId());
        assertEquals(1, afterDelete.size());
    }

    @Test
    void stopRecurring_preventsFutureGeneration() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().minusMinutes(1));

        RecurringTemplateResponse created = recurringTaskService.createRecurring(
                templateTask.getId(), request, adminUser.getEmail());

        recurringTaskService.generateNextOccurrences();
        recurringTaskService.stopRecurring(created.getId(), adminUser.getEmail());
        recurringTaskService.generateNextOccurrences();

        List<RecurringOccurrenceResponse> occurrences = recurringTaskService.getOccurrences(created.getId());
        assertEquals(1, occurrences.size());
    }

    @Test
    void invalidInterval_shouldFail() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(0);
        request.setStartDate(LocalDateTime.now().plusDays(1));

        assertThrows(BadRequestException.class, () ->
                recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail()));
    }

    @Test
    void endsAtBeforeStart_shouldFail() {
        CreateRecurringRequest request = new CreateRecurringRequest();
        request.setRecurrenceType(RecurrenceType.DAILY);
        request.setIntervalValue(1);
        request.setStartDate(LocalDateTime.now().plusDays(10));
        request.setEndsAt(LocalDateTime.now().plusDays(5));

        assertThrows(BadRequestException.class, () ->
                recurringTaskService.createRecurring(templateTask.getId(), request, adminUser.getEmail()));
    }
}

