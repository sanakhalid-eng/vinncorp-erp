package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.request.DepartmentCreateRequest;
import com.vinncorp.erp.modules.hr.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(DepartmentCreateRequest request, Long workspaceId, String actorEmail);

    DepartmentResponse update(Long id, DepartmentCreateRequest request, Long workspaceId, String actorEmail);

    DepartmentResponse get(Long id, Long workspaceId);

    List<DepartmentResponse> list(Long workspaceId, boolean activeOnly);

    void delete(Long id, Long workspaceId, String actorEmail);
}


