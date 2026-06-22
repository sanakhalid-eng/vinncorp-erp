package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.dto.request.EmployeeCreateRequest;
import com.vinncorp.erp.modules.hr.dto.request.EmployeeUpdateRequest;
import com.vinncorp.erp.modules.hr.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse create(EmployeeCreateRequest request, Long workspaceId, String actorEmail);

    EmployeeResponse update(Long id, EmployeeUpdateRequest request, Long workspaceId, String actorEmail);

    EmployeeResponse get(Long id, Long workspaceId);

    List<EmployeeResponse> list(Long workspaceId, Long departmentId, com.vinncorp.erp.modules.hr.enums.EmployeeStatus status);

    void delete(Long id, Long workspaceId, String actorEmail);

    EmployeeResponse getByUserId(Long userId, Long workspaceId);
}


