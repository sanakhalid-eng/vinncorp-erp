package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.request.DesignationCreateRequest;
import com.vinncorp.erp.modules.hr.response.DesignationResponse;

import java.util.List;

public interface DesignationService {

    DesignationResponse create(DesignationCreateRequest request, Long workspaceId, String actorEmail);

    DesignationResponse update(Long id, DesignationCreateRequest request, Long workspaceId, String actorEmail);

    DesignationResponse get(Long id, Long workspaceId);

    List<DesignationResponse> list(Long workspaceId, boolean activeOnly);

    void delete(Long id, Long workspaceId, String actorEmail);
}


