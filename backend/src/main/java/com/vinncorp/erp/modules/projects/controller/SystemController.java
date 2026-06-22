package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.workspace.dto.request.TransferOwnershipRequest;
import com.vinncorp.erp.platform.workspace.dto.response.SystemSettingsResponse;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.ScheduledJob;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.enums.NotificationType;
import com.vinncorp.erp.modules.projects.repository.NotificationRepository;
import com.vinncorp.erp.modules.projects.repository.WebhookDeliveryRepository;
import com.vinncorp.erp.modules.projects.service.*;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.shared.security.WorkspaceOwnerSecurity;
import com.vinncorp.erp.shared.websocket.NotificationWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Tag(name = "System")
public class SystemController {

    private final WorkspaceOwnerSecurity workspaceOwnerSecurity;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final FeatureFlagService featureFlagService;
    private final NotificationService notificationService;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final JobTrackerService jobTrackerService;
    private final RetryService retryService;
    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @GetMapping("/settings")
    @Operation(summary = "Get system settings", description = "Retrieve system settings including feature flags")
    public ResponseEntity<ApiResponse<SystemSettingsResponse>> getSystemSettings(Authentication authentication) {
        boolean isOwner = workspaceOwnerSecurity.isWorkspaceOwner();

        SystemSettingsResponse settings = new SystemSettingsResponse(
                isOwner,
                "PMT-SK",
                featureFlagService.getAllFlags()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "System settings retrieved", settings)
        );
    }

    @Transactional
    @PostMapping("/transfer-ownership")
    @Operation(summary = "Transfer ownership", description = "Transfer workspace ownership to another admin user")
    public ResponseEntity<ApiResponse<Void>> transferOwnership(
            @Valid @RequestBody TransferOwnershipRequest request,
            Authentication authentication
    ) {
        if (!workspaceOwnerSecurity.isWorkspaceOwner()) {
            throw new CustomAccessDeniedException("Only the workspace owner can transfer ownership");
        }

        User currentOwner = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        if (targetUser.getId().equals(currentOwner.getId())) {
            throw new BadRequestException("Cannot transfer ownership to yourself");
        }

        boolean isTargetAdmin = targetUser.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName().equals("ADMIN"));
        if (!isTargetAdmin) {
            throw new BadRequestException("Target user must have the ADMIN role");
        }

        String oldOwnerEmail = currentOwner.getEmail();
        String newOwnerEmail = targetUser.getEmail();

        currentOwner.setWorkspaceOwner(false);
        targetUser.setWorkspaceOwner(true);

        userRepository.save(currentOwner);
        userRepository.save(targetUser);

        activityLogService.logActivity(
                currentOwner.getId(),
                EntityType.SYSTEM,
                targetUser.getId(),
                ActionType.OWNERSHIP_TRANSFERRED,
                Map.of("oldOwner", oldOwnerEmail, "previousOwnerId", currentOwner.getId()),
                Map.of("newOwner", newOwnerEmail, "newOwnerId", targetUser.getId()),
                "Workspace ownership transferred from " + oldOwnerEmail + " to " + newOwnerEmail,
                null
        );

        notificationService.createNotification(
                targetUser.getId(), currentOwner.getId(),
                NotificationType.OWNERSHIP_CHANGED,
                currentOwner.getName() + " transferred workspace ownership to you",
                targetUser.getId(), "SYSTEM", null, null,
                "/settings/system"
        );

        notificationService.createNotification(
                currentOwner.getId(), targetUser.getId(),
                NotificationType.OWNERSHIP_CHANGED,
                "Workspace ownership transferred to " + targetUser.getName(),
                currentOwner.getId(), "SYSTEM", null, null,
                "/settings/system"
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Ownership transferred successfully to " + targetUser.getName(), null)
        );
    }

    @GetMapping("/health")
    @Operation(summary = "Get system health", description = "Retrieve system health metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth(Authentication authentication) {
        Map<String, Object> health = new LinkedHashMap<>();

        long activeUsers = userRepository.countByIsActiveTrue();
        long webhookDeadLetters = webhookDeliveryRepository.countByStatus(com.vinncorp.erp.modules.projects.enums.WebhookDeliveryStatus.DEAD_LETTER);
        long failedJobs = jobTrackerService.getFailedJobCount();
        long queueBacklog = retryService.getQueueBacklog();
        long notificationBacklog = notificationRepository.findAll().stream()
                .filter(n -> !n.isRead())
                .count();
        long activeWebSocketConnections = notificationWebSocketHandler.getActiveConnections();

        health.put("activeUsers", activeUsers);
        health.put("webhookDeadLetters", webhookDeadLetters);
        health.put("failedJobs", failedJobs);
        health.put("queueBacklog", queueBacklog);
        health.put("notificationBacklog", notificationBacklog);
        health.put("activeWebSocketConnections", activeWebSocketConnections);
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("status", "healthy");

        return ResponseEntity.ok(new ApiResponse<>(true, "System health retrieved", health));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Get job statuses", description = "Retrieve statuses of all scheduled jobs")
    public ResponseEntity<ApiResponse<List<ScheduledJob>>> getJobStatuses() {
        List<ScheduledJob> jobs = jobTrackerService.getAllJobs();
        return ResponseEntity.ok(new ApiResponse<>(true, "Job statuses retrieved", jobs));
    }
}



