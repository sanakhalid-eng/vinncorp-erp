package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.response.ActivityLogResponse;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.repository.ActivityLogRepository;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.shared.mapper.PaginationMapper;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Log")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my activity log", description = "Retrieve paginated activity log for the authenticated user")
    public ResponseEntity<ApiResponse<PaginatedResponse<ActivityLogResponse>>> getMyActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        Page<ActivityLogResponse> activities = activityLogService.getActivitiesByUser(user.getId(), pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Activities fetched",
                        PaginationMapper.toPaginatedResponse(activities, activity -> activity)));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Get project activity log", description = "Retrieve paginated activity log for a specific project")
    public ResponseEntity<ApiResponse<PaginatedResponse<ActivityLogResponse>>> getProjectActivities(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        Page<ActivityLogResponse> activities = activityLogService.getActivitiesByProject(projectId, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Activities fetched",
                        PaginationMapper.toPaginatedResponse(activities, activity -> activity)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Get entity activity log", description = "Retrieve paginated activity log filtered by entity type and ID")
    public ResponseEntity<ApiResponse<PaginatedResponse<ActivityLogResponse>>> getEntityActivities(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        Page<ActivityLogResponse> activities = activityLogService.getActivitiesByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Activities fetched",
                        PaginationMapper.toPaginatedResponse(activities, activity -> activity)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(summary = "Get user activity log", description = "Retrieve paginated activity log for a specific user")
    public ResponseEntity<ApiResponse<PaginatedResponse<ActivityLogResponse>>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        Page<ActivityLogResponse> activities = activityLogService.getActivitiesByUser(userId, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Activities fetched",
                        PaginationMapper.toPaginatedResponse(activities, activity -> activity)));
    }

    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get filtered activity log", description = "Retrieve activity log with advanced filters by user, entity, action, date range, and search")
    public ResponseEntity<ApiResponse<PaginatedResponse<ActivityLogResponse>>> getFilteredActivities(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "false") boolean securityOnly,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());

        EntityType entityTypeEnum = entityType != null ? EntityType.valueOf(entityType.toUpperCase()) : null;
        ActionType actionEnum = action != null ? ActionType.valueOf(action.toUpperCase()) : null;

        Page<ActivityLogResponse> activities;
        if (search != null && !search.isBlank()) {
            activities = activityLogRepository.findByMetadataContaining(search, pageable)
                    .map(activityLogService::toResponse);
        } else {
            Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
            activities = activityLogRepository.findByFilters(userId, entityTypeEnum, actionEnum, projectId, workspaceId, startDate, endDate, securityOnly, pageable)
                    .map(activityLogService::toResponse);
        }

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Activities fetched",
                        PaginationMapper.toPaginatedResponse(activities, a -> a))
        );
    }
}



