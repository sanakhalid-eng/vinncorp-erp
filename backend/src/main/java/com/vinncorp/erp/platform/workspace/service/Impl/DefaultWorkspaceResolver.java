package com.vinncorp.erp.platform.workspace.service.impl;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.shared.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultWorkspaceResolver implements CurrentWorkspaceResolver {

    private static final ThreadLocal<Long> CURRENT_WORKSPACE_ID = new ThreadLocal<>();
    private static final long CACHE_TTL = 300_000;

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CacheService cacheService;

    @Override
    public Optional<Workspace> resolveCurrentWorkspace() {
        Long workspaceId = getCurrentWorkspaceId();
        if (workspaceId != null) {
            return resolveWorkspaceById(workspaceId);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Workspace> resolveDefaultWorkspace(Long userId) {
        if (userId == null) return Optional.empty();

        String cacheKey = "default_workspace:" + userId;
        Optional<Long> cachedId = cacheService.get(cacheKey);
        if (cachedId.isPresent()) {
            return resolveWorkspaceById(cachedId.get());
        }

        Optional<WorkspaceMember> firstMember = workspaceMemberRepository
                .findByUserIdAndActiveTrue(userId)
                .stream().findFirst();

        if (firstMember.isPresent()) {
            Long wsId = firstMember.get().getWorkspace().getId();
            cacheService.put(cacheKey, wsId, CACHE_TTL);
            return resolveWorkspaceById(wsId);
        }

        return Optional.empty();
    }

    @Override
    public void setCurrentWorkspace(Long workspaceId) {
        if (workspaceId != null) {
            CURRENT_WORKSPACE_ID.set(workspaceId);
        } else {
            CURRENT_WORKSPACE_ID.remove();
        }
    }

    @Override
    public Long getCurrentWorkspaceId() {
        return CURRENT_WORKSPACE_ID.get();
    }

    public void clear() {
        CURRENT_WORKSPACE_ID.remove();
    }

    private Optional<Workspace> resolveWorkspaceById(Long workspaceId) {
        String cacheKey = "workspace:" + workspaceId;
        Optional<Workspace> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<Workspace> workspace = workspaceRepository.findById(workspaceId);
        workspace.ifPresent(w -> cacheService.put(cacheKey, w, CACHE_TTL));
        return workspace;
    }
}

