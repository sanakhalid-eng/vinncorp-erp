package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.request.SavedSearchRequest;
import com.vinncorp.erp.modules.projects.dto.response.SavedSearchResponse;
import com.vinncorp.erp.modules.projects.entity.SavedSearch;
import com.vinncorp.erp.modules.projects.repository.SavedSearchRepository;
import com.vinncorp.erp.modules.projects.service.SavedSearchService;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedSearchServiceImpl implements SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public List<SavedSearchResponse> list(Long workspaceId, Long userId) {
        requireWorkspace(workspaceId);
        return savedSearchRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(workspaceId, userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SavedSearchResponse create(Long workspaceId, Long userId, SavedSearchRequest request) {
        Workspace workspace = requireWorkspace(workspaceId);
        if (request.isDefault()) {
            clearDefaultFlag(workspaceId, userId);
        }
        SavedSearch search = new SavedSearch();
        search.setWorkspace(workspace);
        search.setUserId(userId);
        search.setName(request.getName());
        search.setQueryText(request.getQueryText());
        search.setFiltersJson(request.getFiltersJson());
        search.setDefault(request.isDefault());
        SavedSearchResponse response = toResponse(savedSearchRepository.save(search));
        evictSearchCaches(workspaceId, userId);
        return response;
    }

    @Override
    @Transactional
    public SavedSearchResponse update(Long workspaceId, Long userId, Long id, SavedSearchRequest request) {
        SavedSearch search = savedSearchRepository
                .findByIdAndWorkspaceIdAndUserIdAndDeletedAtIsNull(id, workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found"));
        if (request.isDefault()) {
            clearDefaultFlag(workspaceId, userId);
        }
        search.setName(request.getName());
        search.setQueryText(request.getQueryText());
        search.setFiltersJson(request.getFiltersJson());
        search.setDefault(request.isDefault());
        SavedSearchResponse response = toResponse(savedSearchRepository.save(search));
        evictSearchCaches(workspaceId, userId);
        return response;
    }

    @Override
    @Transactional
    public void delete(Long workspaceId, Long userId, Long id) {
        SavedSearch search = savedSearchRepository
                .findByIdAndWorkspaceIdAndUserIdAndDeletedAtIsNull(id, workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found"));
        search.softDelete(userId);
        savedSearchRepository.save(search);
        evictSearchCaches(workspaceId, userId);
    }

    private void evictSearchCaches(Long workspaceId, Long userId) {
        cacheService.evict(CacheNames.savedSearch(workspaceId, userId));
        cacheService.evict(CacheNames.analytics(workspaceId));
    }

    private void clearDefaultFlag(Long workspaceId, Long userId) {
        savedSearchRepository.findByWorkspaceIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(workspaceId, userId)
                .forEach(s -> {
                    if (s.isDefault()) {
                        s.setDefault(false);
                        savedSearchRepository.save(s);
                    }
                });
    }

    private Workspace requireWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    private SavedSearchResponse toResponse(SavedSearch search) {
        return SavedSearchResponse.builder()
                .id(search.getId())
                .name(search.getName())
                .queryText(search.getQueryText())
                .filtersJson(search.getFiltersJson())
                .isDefault(search.isDefault())
                .build();
    }
}



