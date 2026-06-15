package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.SavedSearchRequest;
import com.vinncorp.erp.modules.projects.dto.response.SavedSearchResponse;

import java.util.List;

public interface SavedSearchService {

    List<SavedSearchResponse> list(Long workspaceId, Long userId);

    SavedSearchResponse create(Long workspaceId, Long userId, SavedSearchRequest request);

    SavedSearchResponse update(Long workspaceId, Long userId, Long id, SavedSearchRequest request);

    void delete(Long workspaceId, Long userId, Long id);
}



