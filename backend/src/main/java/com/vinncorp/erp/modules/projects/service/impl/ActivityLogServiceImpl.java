package com.vinncorp.erp.modules.projects.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.platform.user.entity.UserSummary;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.response.ActivityLogResponse;
import com.vinncorp.erp.modules.projects.entity.ActivityLog;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.repository.ActivityLogRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final ObjectMapper objectMapper;

    @Override
    public ActivityLogResponse logActivity(
            Long userId,
            EntityType entityType,
            Long entityId,
            ActionType action,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String description,
            Long projectId
    ) {
        return logActivity(userId, entityType, entityId, action, oldValue, newValue, description, projectId, null);
    }

    @Override
    public ActivityLogResponse logActivity(
            Long userId,
            EntityType entityType,
            Long entityId,
            ActionType action,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String description,
            Long projectId,
            Map<String, Object> metadata
    ) {
        ActivityLog log = new ActivityLog();
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            log.setUser(user);
        }
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);

        try {
            if (oldValue != null && !oldValue.isEmpty()) {
                log.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null && !newValue.isEmpty()) {
                log.setNewValue(objectMapper.writeValueAsString(newValue));
            }
        } catch (Exception e) {
            log.setOldValue(oldValue != null ? oldValue.toString() : null);
            log.setNewValue(newValue != null ? newValue.toString() : null);
        }

        log.setDescription(description);

        try {
            if (metadata != null && !metadata.isEmpty()) {
                log.setMetadata(objectMapper.writeValueAsString(metadata));
            }
        } catch (Exception e) {
            log.setMetadata(metadata.toString());
        }

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            log.setProject(project);
        }

        // Resolve workspace context
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (workspaceId == null && projectId != null && log.getProject() != null) {
            workspaceId = log.getProject().getWorkspace().getId();
        }
        if (workspaceId == null) {
            workspaceId = currentWorkspaceResolver.resolveDefaultWorkspace(userId)
                    .map(Workspace::getId)
                    .orElse(null);
        }
        if (workspaceId != null) {
            log.setWorkspaceId(workspaceId);
        }

        ActivityLog saved = activityLogRepository.save(log);
        return toResponse(saved);
    }

    @Override
    public Page<ActivityLogResponse> getActivitiesByProject(Long projectId, Pageable pageable) {
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ActivityLogResponse> getActivitiesByEntity(EntityType entityType, Long entityId, Pageable pageable) {
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ActivityLogResponse> getActivitiesByUser(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public ActivityLogResponse toResponse(ActivityLog log) {
        ActivityLogResponse response = new ActivityLogResponse();
        response.setId(log.getId());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setAction(log.getAction());
        response.setDescription(log.getDescription());
        response.setCreatedAt(log.getCreatedAt());

        if (log.getUser() != null) {
            UserSummary userSummary = new UserSummary();
            userSummary.setId(log.getUser().getId());
            userSummary.setName(log.getUser().getName());
            userSummary.setEmail(log.getUser().getEmail());
            userSummary.setAvatarUrl(log.getUser().getAvatarUrl());
            response.setUser(userSummary);
        }

        try {
            if (log.getOldValue() != null) {
                response.setOldValue(objectMapper.readValue(log.getOldValue(), new TypeReference<>() {}));
            }
            if (log.getNewValue() != null) {
                response.setNewValue(objectMapper.readValue(log.getNewValue(), new TypeReference<>() {}));
            }
            if (log.getMetadata() != null) {
                response.setMetadata(objectMapper.readValue(log.getMetadata(), new TypeReference<>() {}));
            }
        } catch (Exception e) {
            response.setOldValue(null);
            response.setNewValue(null);
            response.setMetadata(null);
        }

        return response;
    }
}



