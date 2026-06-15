package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import com.vinncorp.erp.core.user.entity.UserSummary;

@Data
public class ActivityLogResponse {

    private Long id;
    private UserSummary user;
    private EntityType entityType;
    private Long entityId;
    private ActionType action;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private String description;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}



