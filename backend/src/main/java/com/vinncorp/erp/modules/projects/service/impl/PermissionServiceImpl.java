package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.service.PermissionService;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private static final long CACHE_TTL = 300_000;
    private static final String CACHE_PREFIX = "userProjectPermissions";

    private final ProjectMemberRepository projectMemberRepository;
    private final CacheService cacheService;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;

    @Override
    public boolean hasPermission(Long userId, Long projectId, String permissionName) {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        String cacheKey = buildCacheKey(workspaceId, userId, projectId, permissionName);

        Optional<Boolean> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        boolean result = projectMemberRepository.hasPermission(projectId, userId, permissionName);
        cacheService.put(cacheKey, result, CACHE_TTL);
        return result;
    }

    @Override
    public void evictUserProjectPermissions(Long userId, Long projectId) {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        cacheService.evictByPrefix(buildUserProjectPrefix(workspaceId, userId, projectId));
        log.debug("Evicted permission cache for workspace={} userId={} projectId={}", workspaceId, userId, projectId);
    }

    @Override
    public void evictProjectPermissionsInWorkspace(Long projectId) {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        cacheService.evictByPrefix(buildWorkspacePrefix(workspaceId));
        log.debug("Evicted all permission cache for workspace={} projectId={}", workspaceId, projectId);
    }

    @Override
    public void evictUserPermissions(Long userId) {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        cacheService.evictByPrefix(buildUserPrefix(workspaceId, userId));
        log.debug("Evicted all permission cache for workspace={} userId={}", workspaceId, userId);
    }

    private static String buildCacheKey(Long workspaceId, Long userId, Long projectId, String permissionName) {
        return CACHE_PREFIX + ":ws" + workspaceId + ":u" + userId + ":p" + projectId + ":perm" + permissionName;
    }

    private static String buildUserProjectPrefix(Long workspaceId, Long userId, Long projectId) {
        return CACHE_PREFIX + ":ws" + workspaceId + ":u" + userId + ":p" + projectId + ":";
    }

    private static String buildWorkspacePrefix(Long workspaceId) {
        return CACHE_PREFIX + ":ws" + workspaceId + ":";
    }

    private static String buildUserPrefix(Long workspaceId, Long userId) {
        return CACHE_PREFIX + ":ws" + workspaceId + ":u" + userId + ":";
    }
}



