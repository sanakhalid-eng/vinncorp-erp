package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.core.workspace.entity.WorkspaceRole;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRoleRepository;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;

import com.vinncorp.erp.core.workspace.response.WorkspaceResponse;
import com.vinncorp.erp.core.workspace.response.WorkspaceSettingsResponse;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.core.workspace.request.CreateWorkspaceRequest;
import com.vinncorp.erp.core.workspace.request.WorkspacePreferencesRequest;
import com.vinncorp.erp.core.workspace.response.WorkspaceMemberResponse;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.security.MembershipResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.shared.exception.FileUploadException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceService {

    private static final long CACHE_TTL_MEMBER = 300_000;

    private static final Set<String> RESERVED_SLUGS = Set.of(
        "admin", "api", "system", "root", "null", "undefined",
        "workspace", "workspaces", "personal", "personal-workspace",
        "settings", "invitations", "members",
        "auth", "login", "register", "signup", "signin",
        "help", "support", "docs", "status", "health",
        "favicon", "assets", "static", "uploads"
    );

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final CacheService cacheService;
    private final MembershipResolver membershipResolver;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;
    private final Cloudinary cloudinary;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateExistingUsers() {
        if (workspaceRepository.count() > 0) {
            log.info("Workspaces already exist, skipping migration");
            return;
        }

        log.info("Starting workspace migration for existing users");

        List<User> allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            log.info("No users found, skipping workspace migration");
            return;
        }

        User ownerUser = userRepository.findByWorkspaceOwnerTrue().orElse(allUsers.getFirst());
        List<User> regularUsers = allUsers.stream()
                .filter(u -> !u.getId().equals(ownerUser.getId()))
                .toList();

        Workspace personalWorkspace = new Workspace();
        personalWorkspace.setName("Personal Workspace");
        personalWorkspace.setSlug("personal-workspace");
        personalWorkspace.setDescription("Default personal workspace");
        personalWorkspace.setActive(true);
        personalWorkspace = workspaceRepository.save(personalWorkspace);

        seedDefaultRoles(personalWorkspace);

        WorkspaceMember ownerMember = new WorkspaceMember();
        ownerMember.setWorkspace(personalWorkspace);
        ownerMember.setUser(ownerUser);
        ownerMember.setWorkspaceRole("WORKSPACE_OWNER");
        ownerMember.setJoinedAt(LocalDateTime.now());
        ownerMember.setActive(true);
        workspaceMemberRepository.save(ownerMember);

        for (User user : regularUsers) {
            WorkspaceMember member = new WorkspaceMember();
            member.setWorkspace(personalWorkspace);
            member.setUser(user);
            member.setWorkspaceRole("WORKSPACE_MEMBER");
            member.setJoinedAt(LocalDateTime.now());
            member.setActive(true);
            workspaceMemberRepository.save(member);
        }

        log.info("Workspace migration complete: created '{}' with {} members",
                personalWorkspace.getName(), 1 + regularUsers.size());
    }

    private void seedDefaultRoles(Workspace workspace) {
        List<String[]> defaultRoles = List.of(
            new String[]{"WORKSPACE_OWNER", "Full workspace ownership and control", "true"},
            new String[]{"WORKSPACE_ADMIN", "Workspace administration privileges", "true"},
            new String[]{"WORKSPACE_MEMBER", "Standard workspace member", "true"}
        );

        for (String[] roleDef : defaultRoles) {
            WorkspaceRole role = new WorkspaceRole();
            role.setName(roleDef[0]);
            role.setDescription(roleDef[1]);
            role.setWorkspace(workspace);
            role.setSystemManaged(Boolean.parseBoolean(roleDef[2]));
            role.setPermissionsJson("{}");
            workspaceRoleRepository.save(role);
        }
    }

    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String slug = normalizeSlug(request.getName());
        validateSlug(slug);
        slug = ensureUniqueSlug(slug);

        Workspace workspace = new Workspace();
        workspace.setName(request.getName().trim());
        workspace.setSlug(slug);
        workspace.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        workspace.setActive(true);
        workspace = workspaceRepository.save(workspace);

        seedDefaultRoles(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setWorkspaceRole("WORKSPACE_OWNER");
        member.setJoinedAt(LocalDateTime.now());
        member.setActive(true);
        workspaceMemberRepository.save(member);

        cacheService.evict(CacheNames.defaultWorkspace(userId));

        activityLogService.logActivity(
                userId,
                EntityType.WORKSPACE,
                workspace.getId(),
                ActionType.WORKSPACE_CREATED,
                null,
                Map.of("name", workspace.getName(), "slug", workspace.getSlug()),
                "Workspace '" + workspace.getName() + "' created",
                null
        );

        return toWorkspaceResponse(workspace, member.getWorkspaceRole());
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        long memberCount = workspaceMemberRepository.countByWorkspaceIdAndActiveTrue(workspaceId);

        WorkspaceResponse response = toWorkspaceResponse(workspace, member.getWorkspaceRole());
        response.setMemberCount(memberCount);
        return response;
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceBySlug(String slug, Long userId) {
        Workspace workspace = workspaceRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndActiveTrue(workspace.getId(), userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        long memberCount = workspaceMemberRepository.countByWorkspaceIdAndActiveTrue(workspace.getId());
        WorkspaceResponse response = toWorkspaceResponse(workspace, member.getWorkspaceRole());
        response.setMemberCount(memberCount);
        return response;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces(Long userId) {
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserIdAndActiveTrue(userId);

        return memberships.stream()
                .map(m -> {
                    WorkspaceResponse resp = toWorkspaceResponse(m.getWorkspace(), m.getWorkspaceRole());
                    long count = workspaceMemberRepository.countByWorkspaceIdAndActiveTrue(m.getWorkspace().getId());
                    resp.setMemberCount(count);
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, CreateWorkspaceRequest request, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            workspace.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription().trim());
        }

        workspace = workspaceRepository.save(workspace);
        cacheService.evict(CacheNames.workspace(workspaceId));

        activityLogService.logActivity(
                userId,
                EntityType.WORKSPACE,
                workspaceId,
                ActionType.WORKSPACE_UPDATED,
                null,
                Map.of("name", workspace.getName()),
                "Workspace settings updated",
                null
        );

        return toWorkspaceResponse(workspace, member.getWorkspaceRole());
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!"WORKSPACE_OWNER".equals(member.getWorkspaceRole()) && !user.isWorkspaceOwner()) {
            throw new BadRequestException("Only workspace owners can delete a workspace");
        }

        workspace.softDelete(userId);
        workspaceRepository.save(workspace);
        cacheService.evict(CacheNames.workspace(workspaceId));
        cacheService.evict(CacheNames.workspaceMembers(workspaceId));

        activityLogService.logActivity(
                userId,
                EntityType.WORKSPACE,
                workspaceId,
                ActionType.DELETED,
                Map.of("name", workspace.getName()),
                null,
                "Workspace '" + workspace.getName() + "' deleted",
                null
        );
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getWorkspaceMembers(Long workspaceId, Long userId) {
        workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        return workspaceMemberRepository.findByWorkspaceIdAndActiveTrue(workspaceId).stream()
                .map(m -> WorkspaceMemberResponse.builder()
                        .id(m.getId())
                        .userId(m.getUser().getId())
                        .userName(m.getUser().getName())
                        .userEmail(m.getUser().getEmail())
                        .avatarUrl(m.getUser().getAvatarUrl())
                        .workspaceRole(m.getWorkspaceRole())
                        .joinedAt(m.getJoinedAt())
                        .active(m.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspaceSettingsResponse getWorkspaceSettings(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        WorkspaceSettingsResponse.WorkspacePreferences preferences = parsePreferences(workspace.getSettingsJson());

        WorkspaceSettingsResponse.WorkspaceFeatures features = WorkspaceSettingsResponse.WorkspaceFeatures.builder()
                .slackEnabled(true)
                .webhooksEnabled(true)
                .build();

        WorkspaceSettingsResponse.WorkspaceLimits limits = WorkspaceSettingsResponse.WorkspaceLimits.builder()
                .maxProjects(999999)
                .maxMembers(999999)
                .build();

        return WorkspaceSettingsResponse.builder()
                .workspaceName(workspace.getName())
                .slug(workspace.getSlug())
                .features(features)
                .limits(limits)
                .preferences(preferences)
                .build();
    }

    @Transactional
    public WorkspaceSettingsResponse.WorkspacePreferences updateWorkspacePreferences(
            Long workspaceId, Long userId, WorkspacePreferencesRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        WorkspaceSettingsResponse.WorkspacePreferences prefs = parsePreferences(workspace.getSettingsJson());
        if (request.getTimezone() != null) prefs.setTimezone(request.getTimezone());
        if (request.getDateFormat() != null) prefs.setDateFormat(request.getDateFormat());
        if (request.getWeekStartDay() != null) prefs.setWeekStartDay(request.getWeekStartDay());
        if (request.getDefaultDashboardView() != null) prefs.setDefaultDashboardView(request.getDefaultDashboardView());

        try {
            workspace.setSettingsJson(objectMapper.writeValueAsString(prefs));
            workspaceRepository.save(workspace);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize preferences", e);
        }

        cacheService.evict(CacheNames.workspace(workspaceId));
        return prefs;
    }

    @Transactional
    public String uploadLogo(Long workspaceId, Long userId, MultipartFile file) throws IOException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        if (file.isEmpty()) throw new FileUploadException("File is empty");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new FileUploadException("Only image files are allowed");
        if (file.getSize() > 2 * 1024 * 1024)
            throw new FileUploadException("File size must be less than 2MB");

        try {
            var uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "workspace-logos",
                            "public_id", "workspaces/" + workspaceId,
                            "overwrite", true,
                            "invalidate", true
                    )
            );
            String url = (String) uploadResult.get("secure_url");
            workspace.setLogoUrl(url);
            workspaceRepository.save(workspace);
            cacheService.evict(CacheNames.workspace(workspaceId));
            return url;
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload workspace logo");
        }
    }

    private WorkspaceSettingsResponse.WorkspacePreferences parsePreferences(String settingsJson) {
        if (settingsJson == null || settingsJson.isBlank()) {
            return WorkspaceSettingsResponse.WorkspacePreferences.builder()
                    .timezone("UTC")
                    .dateFormat("YYYY-MM-DD")
                    .weekStartDay("MONDAY")
                    .defaultDashboardView("overview")
                    .build();
        }
        try {
            return objectMapper.readValue(settingsJson, WorkspaceSettingsResponse.WorkspacePreferences.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse workspace settings JSON: {}", e.getMessage());
            return WorkspaceSettingsResponse.WorkspacePreferences.builder()
                    .timezone("UTC")
                    .dateFormat("YYYY-MM-DD")
                    .weekStartDay("MONDAY")
                    .defaultDashboardView("overview")
                    .build();
        }
    }

    @Transactional
    public void removeMember(Long workspaceId, Long targetUserId, Long currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            throw new BadRequestException("Cannot remove yourself from a workspace");
        }

        WorkspaceMember currentMember = workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, currentUserId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        WorkspaceMember targetMember = workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!"WORKSPACE_OWNER".equals(currentMember.getWorkspaceRole())) {
            throw new BadRequestException("Only workspace owners can remove members");
        }

        targetMember.setActive(false);
        workspaceMemberRepository.save(targetMember);

        cacheService.evict(CacheNames.defaultWorkspace(targetUserId));
        cacheService.evict(CacheNames.workspaceMembers(workspaceId));
        cacheService.evict(CacheNames.workspacePermissions(workspaceId, targetUserId));
        permissionService.evictUserPermissions(targetUserId);

        activityLogService.logActivity(
                currentUserId,
                EntityType.WORKSPACE,
                workspaceId,
                ActionType.MEMBER_REMOVED,
                Map.of("userId", targetUserId),
                null,
                "Member removed from workspace",
                null
        );
    }

    @Transactional
    public void switchWorkspace(Long workspaceId, Long userId) {
        Workspace newWorkspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        Workspace previousWorkspace = currentWorkspaceResolver.resolveCurrentWorkspace().orElse(null);

        Map<String, Object> metadata = new HashMap<>();
        if (previousWorkspace != null) {
            metadata.put("previousWorkspaceId", previousWorkspace.getId());
            metadata.put("previousWorkspaceName", previousWorkspace.getName());
        }
        metadata.put("newWorkspaceId", workspaceId);
        metadata.put("newWorkspaceName", newWorkspace.getName());

        currentWorkspaceResolver.setCurrentWorkspace(workspaceId);

        activityLogService.logActivity(
                userId,
                EntityType.WORKSPACE,
                workspaceId,
                ActionType.WORKSPACE_SWITCHED,
                previousWorkspace != null ? Map.of("previousWorkspaceId", previousWorkspace.getId(), "previousWorkspaceName", previousWorkspace.getName()) : null,
                Map.of("newWorkspaceId", workspaceId, "newWorkspaceName", newWorkspace.getName()),
                "Switched to workspace '" + newWorkspace.getName() + "'",
                null,
                metadata
        );
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BadRequestException("Slug cannot be empty");
        }
        if (slug.length() < 2) {
            throw new BadRequestException("Slug must be at least 2 characters");
        }
        if (slug.length() > 100) {
            throw new BadRequestException("Slug must be at most 100 characters");
        }
        if (!slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            throw new BadRequestException("Slug must be lowercase alphanumeric with hyphens only");
        }
        if (RESERVED_SLUGS.contains(slug)) {
            throw new BadRequestException("'" + slug + "' is a reserved slug and cannot be used");
        }
    }

    private WorkspaceResponse toWorkspaceResponse(Workspace workspace, String role) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .description(workspace.getDescription())
                .logoUrl(workspace.getLogoUrl())
                .active(workspace.isActive())
                .role(role)
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }

    static String normalizeSlug(String name) {
        if (name == null || name.isBlank()) return "untitled";
        String slug = name.toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.isEmpty() ? "untitled" : slug;
    }

    private String ensureUniqueSlug(String slug) {
        String candidate = slug;
        int counter = 1;
        while (workspaceRepository.existsBySlug(candidate)) {
            candidate = slug + "-" + counter;
            counter++;
        }
        return candidate;
    }
}



