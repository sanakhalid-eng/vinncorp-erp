package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.repository.UserRepository;

import com.vinncorp.erp.core.user.entity.User;

import com.vinncorp.erp.modules.projects.dto.request.CreateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.request.UpdateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.response.RecurringOccurrenceResponse;
import com.vinncorp.erp.modules.projects.dto.response.RecurringTemplateResponse;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.enums.RecurrenceType;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.RecurringTaskService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTaskServiceImpl implements RecurringTaskService {

    private static final long CACHE_TTL_SECONDS = 300;

    private final RecurringTaskTemplateRepository templateRepository;
    private final RecurringTaskOccurrenceRepository occurrenceRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ActivityLogService activityLogService;
    private final EventPublisher eventPublisher;
    private final CacheService cacheService;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;

    @Override
    @Transactional
    public RecurringTemplateResponse createRecurring(Long taskId, CreateRecurringRequest request, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = getUserByEmail(email);
        Long workspaceId = resolveWorkspaceId();

        if (templateRepository.existsByTemplateTaskIdAndDeletedAtIsNull(taskId)) {
            throw new BadRequestException("Task already has a recurring schedule");
        }

        validateRecurrenceConfig(request);

        RecurringTaskTemplate template = new RecurringTaskTemplate();
        template.setWorkspaceId(workspaceId);
        template.setProjectId(task.getProject().getId());
        template.setTemplateTaskId(taskId);
        template.setRecurrenceType(request.getRecurrenceType());
        template.setIntervalValue(request.getIntervalValue());
        template.setNextRunAt(request.getStartDate());
        template.setCreatedBy(user.getId());

        if (request.getDaysOfWeek() != null) {
            template.setDaysOfWeek(String.join(",", request.getDaysOfWeek()));
        }
        template.setDayOfMonth(request.getDayOfMonth());
        template.setActive(true);
        template.setPaused(false);
        template.setEndsAt(request.getEndsAt());
        template.setMaxOccurrences(request.getMaxOccurrences());
        template.setGeneratedCount(0);

        RecurringTaskTemplate saved = templateRepository.save(template);

        activityLogService.logActivity(
                user.getId(),
                EntityType.RECURRING_TEMPLATE,
                saved.getId(),
                ActionType.RECURRING_TEMPLATE_CREATED,
                null,
                Map.of(
                        "taskId", taskId,
                        "taskTitle", task.getTitle(),
                        "recurrenceType", request.getRecurrenceType().name(),
                        "interval", String.valueOf(request.getIntervalValue())
                ),
                "Recurring schedule created for task: " + task.getTitle(),
                task.getProject().getId()
        );

        evictCache(saved.getId());

        return toTemplateResponse(saved, task.getTitle());
    }

    @Override
    @Transactional
    public RecurringTemplateResponse updateRecurring(Long templateId, UpdateRecurringRequest request, String email) {
        RecurringTaskTemplate template = findTemplate(templateId);
        User user = getUserByEmail(email);
        Task task = taskRepository.findById(template.getTemplateTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Template task not found"));

        Map<String, Object> oldValue = buildTemplateDiff(template);

        if (request.getRecurrenceType() != null) template.setRecurrenceType(request.getRecurrenceType());
        if (request.getIntervalValue() != null) template.setIntervalValue(request.getIntervalValue());
        if (request.getDaysOfWeek() != null) template.setDaysOfWeek(String.join(",", request.getDaysOfWeek()));
        if (request.getDayOfMonth() != null) template.setDayOfMonth(request.getDayOfMonth());
        if (request.getNextRunAt() != null) template.setNextRunAt(request.getNextRunAt());
        if (request.getEndsAt() != null) template.setEndsAt(request.getEndsAt());
        if (request.getMaxOccurrences() != null) template.setMaxOccurrences(request.getMaxOccurrences());

        RecurringTaskTemplate saved = templateRepository.save(template);

        Map<String, Object> newValue = buildTemplateDiff(saved);

        activityLogService.logActivity(
                user.getId(),
                EntityType.RECURRING_TEMPLATE,
                saved.getId(),
                ActionType.RECURRING_TEMPLATE_UPDATED,
                oldValue,
                newValue,
                "Recurring schedule updated for task: " + task.getTitle(),
                task.getProject().getId()
        );

        evictCache(saved.getId());

        return toTemplateResponse(saved, task.getTitle());
    }

    @Override
    public RecurringTemplateResponse getRecurringTemplate(Long templateId) {
        String cacheKey = CacheNames.recurringTemplate(templateId);
        Optional<RecurringTemplateResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        RecurringTaskTemplate template = findTemplate(templateId);
        String taskTitle = resolveTaskTitle(template.getTemplateTaskId());
        RecurringTemplateResponse response = toTemplateResponse(template, taskTitle);
        cacheService.put(cacheKey, response, CACHE_TTL_SECONDS);
        return response;
    }

    @Override
    public List<RecurringTemplateResponse> getTemplatesByProject(Long projectId) {
        List<RecurringTaskTemplate> templates = templateRepository.findByProjectIdAndDeletedAtIsNull(projectId);
        Map<Long, String> taskTitles = new HashMap<>();
        return templates.stream().map(t -> {
            String title = taskTitles.computeIfAbsent(t.getTemplateTaskId(), this::resolveTaskTitle);
            return toTemplateResponse(t, title);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecurringTemplateResponse pauseRecurring(Long templateId, String email) {
        RecurringTaskTemplate template = findTemplate(templateId);
        User user = getUserByEmail(email);
        Task task = taskRepository.findById(template.getTemplateTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Template task not found"));

        if (template.isPaused()) {
            throw new BadRequestException("Recurring schedule is already paused");
        }

        template.setPaused(true);
        RecurringTaskTemplate saved = templateRepository.save(template);

        activityLogService.logActivity(
                user.getId(),
                EntityType.RECURRING_TEMPLATE,
                saved.getId(),
                ActionType.RECURRING_TEMPLATE_PAUSED,
                null,
                null,
                "Recurring schedule paused for task: " + task.getTitle(),
                task.getProject().getId()
        );

        eventPublisher.publish(DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(DomainEvent.Type.RECURRENCE_PAUSED)
                .actorId(user.getId())
                .targetUserId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .entityType("RECURRING_TEMPLATE")
                .entityId(templateId)
                .projectId(task.getProject().getId())
                .projectName(task.getProject() != null && task.getProject().getName() != null ? task.getProject().getName() : null)
                .message("Recurring schedule paused for task: " + task.getTitle())
                .metadata(Map.of("taskId", String.valueOf(task.getId()), "taskTitle", task.getTitle()))
                .build());

        evictCache(saved.getId());

        return toTemplateResponse(saved, task.getTitle());
    }

    @Override
    @Transactional
    public RecurringTemplateResponse resumeRecurring(Long templateId, String email) {
        RecurringTaskTemplate template = findTemplate(templateId);
        User user = getUserByEmail(email);
        Task task = taskRepository.findById(template.getTemplateTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Template task not found"));

        if (!template.isPaused()) {
            throw new BadRequestException("Recurring schedule is not paused");
        }

        template.setPaused(false);
        if (!template.isActive()) {
            template.setActive(true);
        }

        RecurringTaskTemplate saved = templateRepository.save(template);

        activityLogService.logActivity(
                user.getId(),
                EntityType.RECURRING_TEMPLATE,
                saved.getId(),
                ActionType.RECURRING_TEMPLATE_RESUMED,
                null,
                null,
                "Recurring schedule resumed for task: " + task.getTitle(),
                task.getProject().getId()
        );

        evictCache(saved.getId());

        return toTemplateResponse(saved, task.getTitle());
    }

    @Override
    @Transactional
    public void stopRecurring(Long templateId, String email) {
        RecurringTaskTemplate template = findTemplate(templateId);
        User user = getUserByEmail(email);
        Task task = taskRepository.findById(template.getTemplateTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Template task not found"));

        String taskTitle = task.getTitle();

        template.setActive(false);
        templateRepository.save(template);

        activityLogService.logActivity(
                user.getId(),
                EntityType.RECURRING_TEMPLATE,
                templateId,
                ActionType.RECURRING_TEMPLATE_STOPPED,
                null,
                null,
                "Recurring schedule stopped for task: " + taskTitle,
                task.getProject().getId()
        );

        eventPublisher.publish(DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(DomainEvent.Type.RECURRENCE_STOPPED)
                .actorId(user.getId())
                .targetUserId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .entityType("RECURRING_TEMPLATE")
                .entityId(templateId)
                .projectId(task.getProject().getId())
                .projectName(task.getProject() != null && task.getProject().getName() != null ? task.getProject().getName() : null)
                .message("Recurring schedule stopped for task: " + taskTitle)
                .metadata(Map.of("taskId", String.valueOf(task.getId()), "taskTitle", taskTitle))
                .build());

        evictCache(templateId);
    }

    @Override
    public List<RecurringOccurrenceResponse> getOccurrences(Long templateId) {
        String cacheKey = CacheNames.recurringOccurrences(templateId);
        Optional<List<RecurringOccurrenceResponse>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        findTemplate(templateId);
        Map<Long, String> taskTitles = new HashMap<>();

        List<RecurringOccurrenceResponse> responses = occurrenceRepository
                .findByRecurringTemplateIdOrderByOccurrenceDateAsc(templateId)
                .stream()
                .map(o -> {
                    RecurringOccurrenceResponse r = new RecurringOccurrenceResponse();
                    r.setId(o.getId());
                    r.setRecurringTemplateId(o.getRecurringTemplateId());
                    r.setGeneratedTaskId(o.getGeneratedTaskId());
                    r.setOccurrenceDate(o.getOccurrenceDate());
                    r.setGenerationStatus(o.getGenerationStatus());
                    r.setCreatedAt(o.getCreatedAt());
                    String title = taskTitles.computeIfAbsent(o.getGeneratedTaskId(), this::resolveTaskTitle);
                    r.setGeneratedTaskTitle(title);
                    return r;
                })
                .collect(Collectors.toList());

        cacheService.put(cacheKey, responses, CACHE_TTL_SECONDS);
        return responses;
    }

    @Override
    @Transactional
    public void generateNextOccurrences() {
        List<RecurringTaskTemplate> dueTemplates = templateRepository.findDueTemplates(LocalDateTime.now());

        for (RecurringTaskTemplate template : dueTemplates) {
            try {
                generateSingleOccurrence(template);
            } catch (Exception e) {
                log.error("Failed to generate occurrence for template {}: {}", template.getId(), e.getMessage());
            }
        }
    }

    private void generateSingleOccurrence(RecurringTaskTemplate template) {
        if (!template.isActive() || template.isPaused()) return;

        if (template.getEndsAt() != null && LocalDateTime.now().isAfter(template.getEndsAt())) {
            template.setActive(false);
            templateRepository.save(template);
            return;
        }

        if (template.getMaxOccurrences() != null && template.getGeneratedCount() >= template.getMaxOccurrences()) {
            template.setActive(false);
            templateRepository.save(template);
            return;
        }

        LocalDate occurrenceDate = template.getNextRunAt().toLocalDate();

        if (occurrenceRepository.existsByRecurringTemplateIdAndOccurrenceDate(template.getId(), occurrenceDate)) {
            updateNextRunAt(template);
            return;
        }

        Task sourceTask = taskRepository.findById(template.getTemplateTaskId())
                .orElse(null);
        if (sourceTask == null) {
            template.setActive(false);
            templateRepository.save(template);
            return;
        }

        Task generatedTask = cloneTask(sourceTask, occurrenceDate);

        Task savedTask = taskRepository.save(generatedTask);

        cloneDependencies(sourceTask, savedTask, occurrenceDate);

        RecurringTaskOccurrence occurrence = new RecurringTaskOccurrence();
        occurrence.setRecurringTemplateId(template.getId());
        occurrence.setGeneratedTaskId(savedTask.getId());
        occurrence.setOccurrenceDate(occurrenceDate);
        occurrence.setGenerationStatus("GENERATED");
        occurrenceRepository.save(occurrence);

        template.setGeneratedCount(template.getGeneratedCount() + 1);
        template.setLastGeneratedAt(LocalDateTime.now());

        activityLogService.logActivity(
                template.getCreatedBy(),
                EntityType.RECURRING_OCCURRENCE,
                occurrence.getId(),
                ActionType.RECURRING_TASK_GENERATED,
                null,
                Map.of(
                        "templateId", String.valueOf(template.getId()),
                        "generatedTaskId", String.valueOf(savedTask.getId()),
                        "occurrenceDate", occurrenceDate.toString(),
                        "taskTitle", sourceTask.getTitle()
                ),
                "Recurring task generated: " + sourceTask.getTitle() + " (" + occurrenceDate + ")",
                template.getProjectId()
        );

        if (sourceTask.getAssignee() != null) {
            eventPublisher.publish(DomainEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(DomainEvent.Type.RECURRING_TASK_GENERATED)
                    .actorId(template.getCreatedBy())
                    .targetUserId(sourceTask.getAssignee().getId())
                    .entityType("TASK")
                    .entityId(savedTask.getId())
                    .projectId(template.getProjectId())
                    .message("Recurring task generated: " + sourceTask.getTitle() + " (" + occurrenceDate + ")")
                    .metadata(Map.of(
                            "templateId", String.valueOf(template.getId()),
                            "occurrenceDate", occurrenceDate.toString(),
                            "taskTitle", sourceTask.getTitle()
                    ))
                    .build());
        }

        updateNextRunAt(template);

        evictCache(template.getId());
    }

    private Task cloneTask(Task source, LocalDate occurrenceDate) {
        Task clone = new Task();
        clone.setTitle(source.getTitle());
        clone.setDescription(source.getDescription());
        clone.setPriority(source.getPriority());
        clone.setStatusEntity(source.getStatusEntity());
        clone.setProject(source.getProject());
        clone.setColumn(source.getColumn());
        clone.setPosition(source.getPosition());
        clone.setAssignee(source.getAssignee());
        clone.setDueDate(source.getDueDate());
        clone.setParentTask(null);
        clone.setSubtaskCount(0);
        clone.setCompletedSubtaskCount(0);
        clone.setReminderSent(false);

        if (source.getTaskLabels() != null) {
            List<TaskLabel> newLabels = new ArrayList<>();
            for (TaskLabel tl : source.getTaskLabels()) {
                if (tl.getLabel() != null) {
                    TaskLabel newTl = new TaskLabel();
                    newTl.setTask(clone);
                    newTl.setLabel(tl.getLabel());
                    newLabels.add(newTl);
                }
            }
            clone.setTaskLabels(newLabels);
        }

        return clone;
    }

    private void cloneDependencies(Task sourceTask, Task generatedTask, LocalDate occurrenceDate) {
        List<TaskDependency> sourceDeps = taskDependencyRepository.findByTaskIdAndDeletedAtIsNull(sourceTask.getId());
        for (TaskDependency dep : sourceDeps) {
            if (dep.getDependsOnTask() == null) continue;

            RecurringTaskOccurrence depOccurrence = occurrenceRepository
                    .findByRecurringTemplateIdAndOccurrenceDate(
                            findTemplateIdByTaskId(dep.getDependsOnTask().getId()),
                            occurrenceDate)
                    .orElse(null);

            if (depOccurrence != null) {
                TaskDependency newDep = new TaskDependency();
                newDep.setTask(generatedTask);
                newDep.setDependsOnTask(taskRepository.findById(depOccurrence.getGeneratedTaskId()).orElse(null));
                newDep.setDependencyType(dep.getDependencyType());
                newDep.setDescription(dep.getDescription());
                newDep.setCreatedBy(generatedTask.getCreatedBy());
                taskDependencyRepository.save(newDep);
            }
        }
    }

    private Long findTemplateIdByTaskId(Long taskId) {
        List<RecurringTaskTemplate> templates = templateRepository.findByTemplateTaskIdAndDeletedAtIsNull(taskId);
        return templates.isEmpty() ? null : templates.getFirst().getId();
    }

    private void updateNextRunAt(RecurringTaskTemplate template) {
        LocalDateTime next = calculateNextRun(template);
        template.setNextRunAt(next);
        templateRepository.save(template);
    }

    private LocalDateTime calculateNextRun(RecurringTaskTemplate template) {
        LocalDateTime current = template.getNextRunAt();
        return switch (template.getRecurrenceType()) {
            case DAILY -> current.plusDays(template.getIntervalValue());
            case WEEKLY -> calculateNextWeeklyRun(template, current);
            case MONTHLY -> calculateNextMonthlyRun(template, current);
            case CUSTOM -> current.plusDays(template.getIntervalValue());
        };
    }

    private LocalDateTime calculateNextWeeklyRun(RecurringTaskTemplate template, LocalDateTime current) {
        if (template.getDaysOfWeek() == null || template.getDaysOfWeek().isBlank()) {
            return current.plusWeeks(template.getIntervalValue());
        }

        Set<DayOfWeek> selectedDays = Arrays.stream(template.getDaysOfWeek().split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        LocalDate nextDate = current.toLocalDate().plusDays(1);
        int weeksChecked = 0;
        while (weeksChecked < 8) {
            if (selectedDays.contains(nextDate.getDayOfWeek())) {
                return nextDate.atTime(current.toLocalTime());
            }
            nextDate = nextDate.plusDays(1);
            if (nextDate.getDayOfWeek() == DayOfWeek.MONDAY) {
                weeksChecked++;
            }
        }
        return current.plusWeeks(template.getIntervalValue());
    }

    private LocalDateTime calculateNextMonthlyRun(RecurringTaskTemplate template, LocalDateTime current) {
        int day = template.getDayOfMonth() != null ? template.getDayOfMonth() : current.getDayOfMonth();
        LocalDate nextDate = current.toLocalDate().plusMonths(template.getIntervalValue());
        int maxDay = nextDate.lengthOfMonth();
        if (day > maxDay) day = maxDay;
        return nextDate.withDayOfMonth(day).atTime(current.toLocalTime());
    }

    private void validateRecurrenceConfig(CreateRecurringRequest request) {
        if (request.getIntervalValue() < 1) {
            throw new BadRequestException("Interval value must be at least 1");
        }
        if (request.getRecurrenceType() == RecurrenceType.WEEKLY) {
            if (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()) {
                throw new BadRequestException("Days of week are required for WEEKLY recurrence");
            }
            for (String day : request.getDaysOfWeek()) {
                try {
                    DayOfWeek.valueOf(day.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid day of week: " + day);
                }
            }
        }
        if (request.getRecurrenceType() == RecurrenceType.MONTHLY) {
            if (request.getDayOfMonth() == null || request.getDayOfMonth() < 1 || request.getDayOfMonth() > 31) {
                throw new BadRequestException("Day of month must be between 1 and 31");
            }
        }
        if (request.getEndsAt() != null && request.getStartDate() != null && request.getEndsAt().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }
        if (request.getMaxOccurrences() != null && request.getMaxOccurrences() < 1) {
            throw new BadRequestException("Max occurrences must be at least 1");
        }
    }

    private Map<String, Object> buildTemplateDiff(RecurringTaskTemplate template) {
        Map<String, Object> diff = new HashMap<>();
        diff.put("recurrenceType", template.getRecurrenceType().name());
        diff.put("intervalValue", template.getIntervalValue());
        diff.put("daysOfWeek", template.getDaysOfWeek());
        diff.put("dayOfMonth", template.getDayOfMonth());
        diff.put("active", template.isActive());
        diff.put("paused", template.isPaused());
        return diff;
    }

    private RecurringTaskTemplate findTemplate(Long templateId) {
        return templateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring template not found"));
    }

    private String resolveTaskTitle(Long taskId) {
        return taskRepository.findById(taskId)
                .map(Task::getTitle)
                .orElse("Unknown Task");
    }

    private RecurringTemplateResponse toTemplateResponse(RecurringTaskTemplate template, String taskTitle) {
        RecurringTemplateResponse response = new RecurringTemplateResponse();
        response.setId(template.getId());
        response.setWorkspaceId(template.getWorkspaceId());
        response.setProjectId(template.getProjectId());
        response.setTemplateTaskId(template.getTemplateTaskId());
        response.setTemplateTaskTitle(taskTitle);
        response.setRecurrenceType(template.getRecurrenceType());
        response.setIntervalValue(template.getIntervalValue());
        response.setNextRunAt(template.getNextRunAt());
        response.setLastGeneratedAt(template.getLastGeneratedAt());
        response.setActive(template.isActive());
        response.setPaused(template.isPaused());
        response.setEndsAt(template.getEndsAt());
        response.setMaxOccurrences(template.getMaxOccurrences());
        response.setGeneratedCount(template.getGeneratedCount());
        response.setCreatedBy(template.getCreatedBy());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());

        if (template.getDaysOfWeek() != null && !template.getDaysOfWeek().isBlank()) {
            response.setDaysOfWeek(Arrays.asList(template.getDaysOfWeek().split(",")));
        }
        response.setDayOfMonth(template.getDayOfMonth());

        return response;
    }

    @Override
    public void evictCache(Long templateId) {
        cacheService.evict(CacheNames.recurringTemplate(templateId));
        cacheService.evict(CacheNames.recurringOccurrences(templateId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long resolveWorkspaceId() {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (workspaceId == null) {
            throw new BadRequestException("No workspace context available");
        }
        return workspaceId;
    }
}



