package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.ActivityLogResponse;
import com.vinncorp.erp.modules.projects.entity.ActivityLog;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ActivityLogService {

    ActivityLogResponse logActivity(
            Long userId,
            EntityType entityType,
            Long entityId,
            ActionType action,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String description,
            Long projectId
    );

    ActivityLogResponse logActivity(
            Long userId,
            EntityType entityType,
            Long entityId,
            ActionType action,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String description,
            Long projectId,
            Map<String, Object> metadata
    );

    Page<ActivityLogResponse> getActivitiesByProject(Long projectId, Pageable pageable);

    Page<ActivityLogResponse> getActivitiesByEntity(EntityType entityType, Long entityId, Pageable pageable);

    Page<ActivityLogResponse> getActivitiesByUser(Long userId, Pageable pageable);

    ActivityLogResponse toResponse(ActivityLog log);
}



