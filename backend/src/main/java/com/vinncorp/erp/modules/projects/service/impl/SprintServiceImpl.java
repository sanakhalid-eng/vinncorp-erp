package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.request.SprintRequest;
import com.vinncorp.erp.modules.projects.dto.response.SprintResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.mapper.TaskMapper;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.SprintService;
import com.vinncorp.erp.modules.projects.service.VelocityService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final TaskRepository taskRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final EventPublisher eventPublisher;
    private final VelocityService velocityService;

    @Override
    @Transactional
    public SprintResponse createSprint(SprintRequest request, String email) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setStatus(SprintStatus.PLANNED);

        Sprint saved = sprintRepository.save(sprint);

        eventPublisher.publish(DomainEvent.builder()
                .eventId(String.valueOf(saved.getId()))
                .type(DomainEvent.Type.SPRINT_CREATED)
                .actorId(null)
                .entityType("SPRINT")
                .entityId(saved.getId())
                .projectId(project.getId())
                .message("Sprint created: " + saved.getName())
                .build());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public SprintResponse startSprint(Long sprintId, String email) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BadRequestException("Only PLANNED sprints can be started");
        }

        boolean hasActiveSprint = sprintRepository.existsByProjectIdAndStatus(
                sprint.getProject().getId(), "ACTIVE");
        if (hasActiveSprint) {
            throw new BadRequestException("Another sprint is already ACTIVE in this project");
        }

        LocalDate today = LocalDate.now();
        if (sprint.getStartDate() != null && sprint.getStartDate().isBefore(today)) {
            throw new BadRequestException("Warning: Sprint start date is in the past");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        Sprint saved = sprintRepository.save(sprint);

        notifyProjectMembers(saved.getProject().getId(), email,
                "Sprint started: " + saved.getName(),
                DomainEvent.Type.SPRINT_STARTED,
                saved.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public SprintResponse completeSprint(Long sprintId, String email, boolean carryForward) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE sprints can be completed");
        }

        LocalDate today = LocalDate.now();
        if (sprint.getEndDate() != null && sprint.getEndDate().isAfter(today)) {
            throw new BadRequestException("Warning: Sprint end date is in the future. Consider if you want to complete early.");
        }

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintIdWithTasks(sprint.getId());
        int totalTasks = taskSprints.size();
        int completedTasks = 0;
        int carriedForwardCount = 0;

        WorkflowStatus workflowStatus = workflowStatusRepository
                .findByIsDefaultTrueAndProjectId(sprint.getProject().getId())
                .orElse(null);

        for (TaskSprint ts : taskSprints) {
            if (ts.getTask() != null && ts.getTask().getStatusEntity() != null
                    && workflowStatus != null
                    && ts.getTask().getStatusEntity().getId().equals(workflowStatus.getId())) {
                completedTasks++;
            } else if (carryForward && ts.getTask() != null) {
                taskSprintRepository.delete(ts);
                carriedForwardCount++;
            }
        }

        sprint.setSummaryTotalTasks(totalTasks);
        sprint.setSummaryCompletedTasks(completedTasks);
        sprint.setSummaryCarriedForward(carriedForwardCount);
        sprint.setCompletedAt(LocalDateTime.now());
        sprint.setStatus(SprintStatus.COMPLETED);

        Sprint saved = sprintRepository.save(sprint);

        try {
            velocityService.generateVelocitySnapshot(sprint.getId());
        } catch (Exception e) {
            // non-blocking: velocity snapshot generation failure should not fail sprint completion
        }

        String msg = "Sprint completed: " + saved.getName() + " (" + completedTasks + "/" + totalTasks + " tasks done)";
        notifyProjectMembers(saved.getProject().getId(), email,
                msg,
                DomainEvent.Type.SPRINT_COMPLETED,
                saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public SprintResponse completeSprint(Long sprintId, String email) {
        return completeSprint(sprintId, email, false);
    }

    @Override
    public List<TaskResponse> getBacklogTasks(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        return taskRepository.findBacklogTasks(projectId).stream()
                .map(TaskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SprintResponse> getProjectSprints(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        return sprintRepository.findByProjectIdOrderByStartDateDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SprintResponse getActiveSprint(Long projectId) {
        return sprintRepository.findActiveSprintByProjectId(projectId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public SprintResponse getSprintById(Long sprintId) {
        return sprintRepository.findById(sprintId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
    }

    @Override
    @Transactional
    public void deleteSprint(Long sprintId, String email) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        taskSprintRepository.deleteBySprintId(sprintId);
        sprintRepository.delete(sprint);
    }

    private SprintResponse toResponse(Sprint sprint) {
        SprintResponse response = new SprintResponse();
        response.setId(sprint.getId());
        response.setProjectId(sprint.getProject() != null ? sprint.getProject().getId() : null);
        response.setProjectName(sprint.getProject() != null ? sprint.getProject().getName() : null);
        response.setName(sprint.getName());
        response.setGoal(sprint.getGoal());
        response.setStartDate(sprint.getStartDate());
        response.setEndDate(sprint.getEndDate());
        response.setStatus(sprint.getStatus() != null ? sprint.getStatus().name() : null);
        response.setCreatedAt(sprint.getCreatedAt());
        response.setUpdatedAt(sprint.getUpdatedAt());

        List<TaskSprint> taskSprints = taskSprintRepository.findBySprintId(sprint.getId());
        int total = taskSprints.size();
        int completed = 0;

        if (total > 0 && sprint.getProject() != null) {
            WorkflowStatus workflowStatus = workflowStatusRepository.findByIsDefaultTrueAndProjectId(sprint.getProject().getId())
                    .orElse(null);
            if (workflowStatus != null) {
                completed = (int) taskSprints.stream()
                        .filter(ts -> ts.getTask() != null
                                && ts.getTask().getStatusEntity() != null
                                && ts.getTask().getStatusEntity().getId().equals(workflowStatus.getId()))
                        .count();
            }
        }

        response.setTotalTasks(total);
        response.setCompletedTasks(completed);
        response.setProgressPercentage(total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0.0);
        response.setSummaryTotalTasks(sprint.getSummaryTotalTasks());
        response.setSummaryCompletedTasks(sprint.getSummaryCompletedTasks());
        response.setSummaryCarriedForward(sprint.getSummaryCarriedForward());
        response.setCompletedAt(sprint.getCompletedAt() != null ? sprint.getCompletedAt().toLocalDate() : null);

        return response;
    }

    private void notifyProjectMembers(Long projectId, String actorEmail, String message, DomainEvent.Type eventType, Long sprintId) {
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);
        ProjectMember actorMember = projectMemberRepository.findByProject_IdAndUser_Email(projectId, actorEmail).orElse(null);
        Long actorId = actorMember != null && actorMember.getUser() != null ? actorMember.getUser().getId() : null;

        for (ProjectMember member : members) {
            if (member.getUser() != null && !member.getUser().getEmail().equals(actorEmail)) {
                eventPublisher.publish(DomainEvent.builder()
                        .eventId(String.valueOf(sprintId))
                        .type(eventType)
                        .actorId(actorId)
                        .targetUserId(member.getUser().getId())
                        .entityType("SPRINT")
                        .entityId(sprintId)
                        .projectId(projectId)
                        .message(message)
                        .build());
            }
        }
    }
}



