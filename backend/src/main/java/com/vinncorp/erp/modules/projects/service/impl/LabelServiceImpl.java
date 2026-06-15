package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.constants.PermissionConstants;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.request.LabelRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskLabelAssignmentRequest;
import com.vinncorp.erp.modules.projects.dto.response.LabelResponse;
import com.vinncorp.erp.modules.projects.entity.Label;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskLabel;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.LabelService;
import com.vinncorp.erp.modules.projects.util.ColorValidator;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ForbiddenOperationException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public LabelResponse createLabel(Long projectId, LabelRequest request, String email) {
        User user = getUserByEmail(email);

        if (!hasCreatePermission(projectId, user.getId())) {
            throw new ForbiddenOperationException("You do not have permission to create labels");
        }

        if (labelRepository.existsByNameAndProjectIdActive(request.getName().trim(), projectId)) {
            throw new BadRequestException("A label with this name already exists in the project");
        }

        ColorValidator.validate(request.getColor());

        Label label = new Label();
        label.setName(request.getName().trim());
        label.setColor(request.getColor().toUpperCase());
        label.setProject(projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found")));

        Label saved = labelRepository.save(label);

        activityLogService.logActivity(
                user.getId(),
                EntityType.PROJECT,
                projectId,
                ActionType.UPDATED,
                null,
                Map.of("labelName", saved.getName(), "labelColor", saved.getColor()),
                "Label created: " + saved.getName(),
                projectId
        );

        return toResponse(saved);
    }

    @Override
    public List<LabelResponse> getLabelsByProject(Long projectId) {
        return labelRepository.findByProjectIdActive(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteLabel(Long labelId, String email) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        User user = getUserByEmail(email);

        if (!hasDeletePermission(label.getProject().getId(), user.getId())) {
            throw new ForbiddenOperationException("You do not have permission to delete labels");
        }

        if (label.isDeleted()) {
            throw new BadRequestException("Label is already deleted");
        }

        Long projectId = label.getProject().getId();
        String labelName = label.getName();

        label.softDelete();
        labelRepository.save(label);

        activityLogService.logActivity(
                user.getId(),
                EntityType.PROJECT,
                projectId,
                ActionType.DELETED,
                Map.of("labelName", labelName),
                null,
                "Label deleted: " + labelName,
                projectId
        );
    }

    @Override
    @Transactional
    public List<LabelResponse> assignLabelsToTask(Long taskId, TaskLabelAssignmentRequest request, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = getUserByEmail(email);

        if (hasAssignPermission(task.getProject().getId(), user.getId())) {
            throw new ForbiddenOperationException("You do not have permission to assign labels");
        }

        Long projectId = task.getProject().getId();

        List<Label> labels = labelRepository.findByIdInAndProjectIdActive(request.getLabelIds(), projectId);
        if (labels.size() != request.getLabelIds().size()) {
            throw new BadRequestException("Some labels were not found or do not belong to this project");
        }

        List<LabelResponse> addedLabels = new ArrayList<>();
        for (Label label : labels) {
            if (!taskLabelRepository.existsByTaskIdAndLabelId(taskId, label.getId())) {
                TaskLabel taskLabel = new TaskLabel();
                taskLabel.setTask(task);
                taskLabel.setLabel(label);
                taskLabelRepository.save(taskLabel);
                addedLabels.add(toResponse(label));

                activityLogService.logActivity(
                        user.getId(),
                        EntityType.TASK,
                        taskId,
                        ActionType.UPDATED,
                        null,
                        Map.of("labelId", label.getId(), "labelName", label.getName(), "labelColor", label.getColor()),
                        "Label \"" + label.getName() + "\" added to task: " + task.getTitle(),
                        projectId
                );

                if (task.getAssignee() != null && !task.getAssignee().getId().equals(user.getId())) {
                    eventPublisher.publish(DomainEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .type(DomainEvent.Type.LABEL_ADDED_TO_TASK)
                            .actorId(user.getId())
                            .targetUserId(task.getAssignee().getId())
                            .entityType("TASK")
                            .entityId(taskId)
                            .projectId(projectId)
                            .projectName(task.getProject().getName())
                            .message("Label \"" + truncate(label.getName(), 20) + "\" added to task: " + truncate(task.getTitle(), 30))
                            .metadata(Map.of("labelId", label.getId(), "labelName", label.getName(), "labelColor", label.getColor()))
                            .build());
                }
            }
        }

        return addedLabels;
    }

    @Override
    @Transactional
    public void removeLabelFromTask(Long taskId, Long labelId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = getUserByEmail(email);

        if (hasAssignPermission(task.getProject().getId(), user.getId())) {
            throw new ForbiddenOperationException("You do not have permission to remove labels");
        }

        TaskLabel taskLabel = taskLabelRepository.findByTaskIdAndLabelId(taskId, labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label is not assigned to this task"));

        Label label = taskLabel.getLabel();
        String labelName = label.getName();

        taskLabelRepository.delete(taskLabel);

        activityLogService.logActivity(
                user.getId(),
                EntityType.TASK,
                taskId,
                ActionType.UPDATED,
                Map.of("labelId", labelId, "labelName", labelName, "labelColor", label.getColor()),
                null,
                "Label \"" + labelName + "\" removed from task: " + task.getTitle(),
                task.getProject().getId()
        );

        eventPublisher.publish(DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(DomainEvent.Type.LABEL_REMOVED_FROM_TASK)
                .actorId(user.getId())
                .targetUserId(task.getAssignee() != null ? task.getAssignee().getId() : task.getCreator().getId())
                .entityType("TASK")
                .entityId(taskId)
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .message("Label \"" + truncate(labelName, 20) + "\" removed from task: " + truncate(task.getTitle(), 30))
                .metadata(Map.of("labelId", labelId, "labelName", labelName))
                .build());
    }

    @Override
    @Transactional
    public int removeLabelsFromTask(Long taskId, List<Long> labelIds, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = getUserByEmail(email);

        if (hasAssignPermission(task.getProject().getId(), user.getId())) {
            throw new ForbiddenOperationException("You do not have permission to remove labels");
        }

        Long projectId = task.getProject().getId();
        int removedCount = 0;

        for (Long labelId : labelIds) {
            Optional<TaskLabel> opt = taskLabelRepository.findByTaskIdAndLabelId(taskId, labelId);
            if (opt.isPresent()) {
                TaskLabel taskLabel = opt.get();
                Label label = taskLabel.getLabel();

                activityLogService.logActivity(
                        user.getId(),
                        EntityType.TASK,
                        taskId,
                        ActionType.UPDATED,
                        Map.of("labelId", labelId, "labelName", label.getName()),
                        null,
                        "Label \"" + label.getName() + "\" removed from task: " + task.getTitle(),
                        projectId
                );

                eventPublisher.publish(DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .type(DomainEvent.Type.LABEL_REMOVED_FROM_TASK)
                        .actorId(user.getId())
                .targetUserId(task.getAssignee() != null ? task.getAssignee().getId() : task.getCreator().getId())
                        .entityType("TASK")
                        .entityId(taskId)
                        .projectId(projectId)
                        .projectName(task.getProject().getName())
                        .message("Label \"" + truncate(label.getName(), 20) + "\" removed from task: " + truncate(task.getTitle(), 30))
                        .metadata(Map.of("labelId", labelId, "labelName", label.getName()))
                        .build());

                taskLabelRepository.delete(taskLabel);
                removedCount++;
            }
        }

        return removedCount;
    }

    @Override
    public List<LabelResponse> getLabelsForTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }

        return taskLabelRepository.findByTaskIdWithActiveLabel(taskId)
                .stream()
                .map(tl -> toResponse(tl.getLabel()))
                .collect(Collectors.toList());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean hasCreatePermission(Long projectId, Long userId) {
        return hasPermission(projectId, userId, PermissionConstants.CREATE_LABEL);
    }

    private boolean hasDeletePermission(Long projectId, Long userId) {
        return hasPermission(projectId, userId, PermissionConstants.DELETE_LABEL);
    }

    private boolean hasAssignPermission(Long projectId, Long userId) {
        return !hasPermission(projectId, userId, PermissionConstants.ASSIGN_LABEL);
    }

    private boolean hasPermission(Long projectId, Long userId, String permission) {
        return projectMemberRepository.hasPermission(projectId, userId, permission);
    }

    private LabelResponse toResponse(Label label) {
        LabelResponse response = new LabelResponse();
        response.setId(label.getId());
        response.setName(label.getName());
        response.setColor(label.getColor());
        response.setProjectId(label.getProject().getId());
        response.setProjectName(label.getProject().getName());
        response.setCreatedAt(label.getCreatedAt());
        response.setUsageCount(label.getUsageCount());
        return response;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}



