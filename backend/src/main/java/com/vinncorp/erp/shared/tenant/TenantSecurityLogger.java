package com.vinncorp.erp.shared.tenant;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSecurityLogger {

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;

    public void logCrossWorkspaceAccess(Long userId, String attemptedWorkspaceId, String entityType, Long entityId) {
        Long currentWsId = currentWorkspaceResolver.getCurrentWorkspaceId();
        String message = "Cross-workspace access attempt: user=" + userId
                + " attemptedWorkspace=" + attemptedWorkspaceId
                + " currentWorkspace=" + currentWsId
                + " entityType=" + entityType
                + " entityId=" + entityId;
        log.warn(message);

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        activityLogService.logActivity(
                userId,
                EntityType.SYSTEM,
                entityId,
                ActionType.SECURITY_VALIDATION_FAILED,
                java.util.Map.of(
                    "attemptedWorkspaceId", attemptedWorkspaceId,
                    "currentWorkspaceId", String.valueOf(currentWsId),
                    "entityType", entityType
                ),
                null,
                message,
                null
        );
    }

    public void logInvalidWorkspaceHeader(String headerValue, String remoteAddr) {
        log.warn("Invalid X-Workspace-Id header: value={}, remoteAddr={}", headerValue, remoteAddr);
    }

    public void logWorkspaceSpoofAttempt(Long userId, Long claimedWorkspaceId, String description) {
        log.warn("Workspace spoof attempt: user={}, claimedWorkspace={}, description={}",
                userId, claimedWorkspaceId, description);
    }
}


